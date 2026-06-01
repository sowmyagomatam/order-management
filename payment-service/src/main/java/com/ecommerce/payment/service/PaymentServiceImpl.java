package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.domain.command.ProcessPaymentCommand;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.external.PaymentGateway;
import com.ecommerce.payment.external.PaymentResult;
import com.ecommerce.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final int GATEWAY_TIMEOUT_SECONDS = 10;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ExecutorService paymentExecutor;

    /**
     * Create payment for an order
     * @param createPaymentCommand
     * @return
     */
    @Override
    @Transactional
    public Payment createPayment(CreatePaymentCommand createPaymentCommand) {
        log.info("Creating payment for order: {}", createPaymentCommand.orderId());

        //Check idempotency to see if a payment already exists
        Optional<Payment> existing = paymentRepository.findByOrderId(createPaymentCommand.orderId());
        if (existing.isPresent()) {
            log.info("Payment already exists for order: {}", createPaymentCommand.orderId());
            return existing.get();
        }
        Payment payment = Payment.builder()
                .orderId(createPaymentCommand.orderId())
                .amount(createPaymentCommand.amount())
                .paymentMethod(createPaymentCommand.paymentMethod())
                .cardLastFourDigits(createPaymentCommand.cardLastFourDigits())
                .build();
        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: {} for order: {}", saved.getId(), saved.getOrderId());

        return saved;
    }

    @Override
    @Transactional
    public Payment processPayment(ProcessPaymentCommand processPaymentCommand) {

        log.info("Processing payment: {}", processPaymentCommand.paymentId());

        Payment payment = paymentRepository.findById(processPaymentCommand.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(processPaymentCommand.paymentId()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING &&
                payment.getPaymentStatus() != PaymentStatus.PROCESSING) {
            log.warn("Payment {} in status {}, cannot process",
                    payment.getId(), payment.getPaymentStatus());
            return payment;
        }

        // Mark as processing and persist immediately for visibility
        payment.markAsProcessing();
        Payment processing = paymentRepository.saveAndFlush(payment);
        log.debug("Payment {} marked as processing", processing.getId());
        try {
            // Call payment gateway
            PaymentResult result = executeWithTimeout(
                            payment.getAmount(),
                            payment.getPaymentMethod()
                    );

            if (result.success()) {
                processing.markAsCompleted(result.paymentReference());
                log.info("Payment {} completed. Reference: {}",
                        processing.getId(), result.paymentReference());
            } else {
                processing.markAsFailed(result.failureReason());
                log.warn("Payment {} failed. Reason: {}",
                        processing.getId(), result.failureReason());
            }

        } catch (TimeoutException e) {
            log.error("Payment {} timed out after {} seconds",
                    processing.getId(), GATEWAY_TIMEOUT_SECONDS);
            processing.markAsFailed("Payment gateway timeout");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment {} processing was interrupted", processing.getId());
            processing.markAsFailed("Payment processing interrupted");

        } catch (ExecutionException e) {
            log.error("Gateway execution error for payment {}", processing.getId(), e.getCause());
            String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            processing.markAsFailed("Gateway error: " + errorMessage);

        } catch (Exception e) {
            log.error("Unexpected error processing payment {}", processing.getId(), e);
            processing.markAsFailed("Unexpected error: " + e.getMessage());
        }

        // Save final state
        Payment finalPayment = paymentRepository.save(processing);
        log.info("Payment {} processing completed with final status: {}",
                finalPayment.getId(), finalPayment.getPaymentStatus());

        return finalPayment;
    }

    private PaymentResult executeWithTimeout(BigDecimal amount, PaymentMethod method) throws ExecutionException, InterruptedException, TimeoutException {
        log.debug("Submitting payment gateway task with {} second timeout", GATEWAY_TIMEOUT_SECONDS);

        // Submit gateway call to executor
        Future<PaymentResult> future = paymentExecutor.submit(() ->
                paymentGateway.processPayment(amount, method)
        );

        try {
            // Wait for result with timeout
            return future.get(GATEWAY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // Cancel the task if it times out
            boolean cancelled = future.cancel(true);
            log.warn("Payment gateway task timed out and was {}cancelled",
                    cancelled ? "" : "NOT ");
            throw e;
        }
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}

package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.domain.command.ProcessPaymentCommand;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.external.PaymentGateway;
import com.ecommerce.payment.external.PaymentResult;
import com.ecommerce.payment.repository.PaymentRepository;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final int GATEWAY_TIMEOUT = 5;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    @Transactional
    public Payment createPayment(CreatePaymentCommand createPaymentCommand) {
        log.info("Creating payment for order: {}", createPaymentCommand.orderId());

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

        // Mark as processing
        payment.markAsProcessing();
        Payment processing = paymentRepository.saveAndFlush(payment);
        log.debug("Payment {} marked as processing", processing.getId());
        try {
            // Call payment gateway
            PaymentResult result = executeWithTimeout(() ->
                    paymentGateway.processPayment(
                            payment.getAmount(),
                            payment.getPaymentMethod()
                    ));

            if (result.success()) {
                processing.markAsCompleted(result.paymentReference());
                log.info("Payment {} completed. Reference: {}",
                        processing.getId(), result.paymentReference());
            } else {
                processing.markAsFailed(result.failureReason());
                log.warn("Payment {} failed. Reason: {}",
                        processing.getId(), result.failureReason());
            }

        } catch (TimeoutException | InterruptedException e){
            log.error("Payment {} interrupted out", payment.getId());
            payment.markAsFailed("Payment processing interrrupted");
        } catch (ExecutionException e){
            log.error("Payment {} timed out", payment.getId());
            payment.markAsFailed("Payment gateway timeout");
        }
        catch (Exception e) {
            log.error("Error processing payment {}", processing.getId(), e);
            processing.markAsFailed("Gateway error: " + e.getMessage());
        }

        return paymentRepository.save(processing);
    }

    private <T> T executeWithTimeout(Callable<T> callable) throws ExecutionException, InterruptedException, TimeoutException {
        Future<T> future = executorService.submit(callable);
        try {
           return future.get(GATEWAY_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            future.cancel(true);
            throw e;
        }
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}

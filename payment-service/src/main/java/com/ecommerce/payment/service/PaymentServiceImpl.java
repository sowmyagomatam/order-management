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

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

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
        return paymentRepository.save(payment);
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
        paymentRepository.save(payment);

        try {
            // Call payment gateway
            PaymentResult result = paymentGateway.processPayment(
                    payment.getAmount(),
                    payment.getPaymentMethod()
            );

            if (result.success()) {
                payment.markAsCompleted(result.paymentReference());
                log.info("Payment {} completed. Reference: {}",
                        payment.getId(), result.paymentReference());
            } else {
                payment.markAsFailed(result.failureReason());
                log.warn("Payment {} failed. Reason: {}",
                        payment.getId(), result.failureReason());
            }

        } catch (Exception e) {
            log.error("Error processing payment {}", payment.getId(), e);
            payment.markAsFailed("Gateway error: " + e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}

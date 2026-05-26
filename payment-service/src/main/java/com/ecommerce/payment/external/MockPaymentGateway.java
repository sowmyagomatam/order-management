package com.ecommerce.payment.external;

import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.exception.PaymentServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final int MIN_DELAY_MS = 1000;
    private static final int MAX_ADDITIONAL_DELAY_MS = 2000;
    private static final int SUCCESS_RATE_PERCENT = 90;
    private final String[] FAILURE_REASONS = new String[]{
            "Insufficient funds",
            "Card declined",
            "Payment gateway timeout",
            "Invalid card number"
    };

    /**
     * Simulates a payment process
     * @param amount
     * @param paymentMethod
     * @return Payment result success or failure
     */
    @Override
    public PaymentResult processPayment(BigDecimal amount, PaymentMethod paymentMethod) {

        log.info("Processing payment: amount={}, method={}", amount, paymentMethod);

        try {
            // Simulate processing delay - 1 to 3 seconds
            simulateProcessingDelay();

            // Simulate payment outcome (90% success, 10% failure)
            return simulatePaymentOutcome();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore interrupt flag
            log.error("Payment processing was interrupted", e);
            throw new PaymentServiceException("Payment processing interrupted", "PAYMENT_INTERRUPTED");
        }
    }

    private void simulateProcessingDelay() throws InterruptedException {
        int delayMs = MIN_DELAY_MS + ThreadLocalRandom.current().nextInt(MAX_ADDITIONAL_DELAY_MS);
        log.debug("Simulating payment gateway delay: {}ms", delayMs);
        Thread.sleep(delayMs);
    }

    private PaymentResult simulatePaymentOutcome() {
        // Random number 0-99
        int random = ThreadLocalRandom.current().nextInt(100);

        if (random < SUCCESS_RATE_PERCENT) {
            // Success (90% of the time)
            String reference = generatePaymentReference();
            log.info("Payment succeeded: reference={}", reference);
            return PaymentResult.success(reference);

        } else {
            // Failure (10% of the time)
            String reason = selectRandomFailureReason();
            log.warn("Payment failed: reason={}", reason);
            return PaymentResult.failure(reason);
        }
    }

    /**
     * Generate unique payment reference (simulates gateway transaction ID)
     */
    private String generatePaymentReference() {
        return "ch_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Select random failure reason from predefined list
     */
    private String selectRandomFailureReason() {
        int index = ThreadLocalRandom.current().nextInt(FAILURE_REASONS.length);
        return FAILURE_REASONS[index];
    }
}


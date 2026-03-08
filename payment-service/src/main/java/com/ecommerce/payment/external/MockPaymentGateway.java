package com.ecommerce.payment.external;

import com.ecommerce.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {
    private final Random random = new Random();
    private final String[] failureReasons = new String[]{
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
        // Simulate processing delay - 1 to 3 seconds
        try {
            Thread.sleep(1000 + random.nextInt(2000));
            if(random.nextInt(100) < 90){
                return PaymentResult.success("ch_" + UUID.randomUUID().toString().substring(0, 8));
            } else {

                return PaymentResult.failure(failureReasons[random.nextInt(failureReasons.length)]);
            }
        } catch (InterruptedException e) {
            return PaymentResult.failure("Payment gateway timeout");
        }


    }
}

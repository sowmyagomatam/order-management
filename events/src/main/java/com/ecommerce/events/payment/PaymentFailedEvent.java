package com.ecommerce.events.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFailedEvent(String paymentId,
                                 String orderId,
                                 BigDecimal amount,
                                 String failureReason,
                                 Instant timestamp
) {
    public PaymentFailedEvent{
        if(paymentId == null || paymentId.isBlank()){
            throw new IllegalArgumentException("Payment Id cannot be null or blank");
        }
        if(orderId == null || orderId.isBlank()){
            throw new IllegalArgumentException("Order Id cannot be null or blank");
        }
        if(failureReason == null || failureReason.isBlank()){
            throw new IllegalArgumentException("Failure reason cannot be null or blank");
        }
        if(timestamp == null){
            timestamp = Instant.now();
        }
    }
}

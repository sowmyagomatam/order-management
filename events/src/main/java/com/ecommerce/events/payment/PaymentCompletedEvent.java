package com.ecommerce.events.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(String paymentId,
                                    String orderId,
                                    BigDecimal amount,
                                    String paymentMethod,
                                    String paymentReference,
                                    Instant timestamp) {

    public PaymentCompletedEvent{
        if(paymentId == null || paymentId.isBlank()){
            throw new IllegalArgumentException("Payment Id cannot be null or blank");
        }
        if(orderId == null || orderId.isBlank()){
            throw new IllegalArgumentException("Order Id cannot be null or blank");
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        if(timestamp == null){
            timestamp = Instant.now();
        }

    }
}

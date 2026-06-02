package com.ecommerce.events.payment;

import java.time.Instant;

public record PaymentProcessingEvent(String paymentId,
                                     String orderId,
                                     Instant timestamp) {
    public PaymentProcessingEvent {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment Id cannot be null or blank");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order Id cannot be null or blank");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

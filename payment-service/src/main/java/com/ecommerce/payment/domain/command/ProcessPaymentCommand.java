package com.ecommerce.payment.domain.command;

import lombok.Builder;

@Builder
public record ProcessPaymentCommand(
        String paymentId
) {
    public ProcessPaymentCommand {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be null or blank");
        }
    }
}
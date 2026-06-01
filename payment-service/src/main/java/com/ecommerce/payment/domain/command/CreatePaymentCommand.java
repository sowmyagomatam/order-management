package com.ecommerce.payment.domain.command;

import com.ecommerce.payment.domain.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatePaymentCommand(
        String orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String cardLastFourDigits
) {
    public CreatePaymentCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
    }
}

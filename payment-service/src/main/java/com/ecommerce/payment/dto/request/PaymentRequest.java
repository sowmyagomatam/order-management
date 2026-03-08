package com.ecommerce.payment.dto.request;

import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Builder
public record PaymentRequest(
    @NotBlank(message = "Order Id is required")
    String orderId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount  must be positive")
    BigDecimal amount,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @Pattern(regexp = "^\\d{4}$", message = "Card last four digits must be exactly 4 digits")
    String cardLastFourDigits)
{
    public PaymentRequest {
        if (cardLastFourDigits != null && cardLastFourDigits.length() != 4) {
            throw new IllegalArgumentException("Card last four digits must be exactly 4 characters");
        }
    }
}

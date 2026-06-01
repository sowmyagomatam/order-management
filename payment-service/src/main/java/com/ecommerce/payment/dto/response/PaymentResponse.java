package com.ecommerce.payment.dto.response;

import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Builder
public record PaymentResponse(
        String id,
        String orderId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String paymentReference,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String cardLastFourDigits,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {

}
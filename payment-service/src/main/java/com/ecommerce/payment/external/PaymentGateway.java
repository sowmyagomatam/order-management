package com.ecommerce.payment.external;

import com.ecommerce.payment.domain.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult processPayment(BigDecimal amount, PaymentMethod paymentMethod);
}

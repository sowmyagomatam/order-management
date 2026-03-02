package com.ecommerce.payment.exception;

import com.ecommerce.common.exception.BaseServiceException;
import com.ecommerce.payment.domain.PaymentStatus;

public class InvalidPaymentStateException extends BaseServiceException {
    public InvalidPaymentStateException(String id,
                                        PaymentStatus status,
                                        PaymentStatus newStatus) {
        super(String.format("Cannot transition payment %s from %s to %s",
                        id, status, newStatus),
                "INVALID_PAYMENT");

    }
}

package com.ecommerce.payment.exception;

import com.ecommerce.common.exception.BaseServiceException;

public class PaymentServiceException extends BaseServiceException {

    public PaymentServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public PaymentServiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

}

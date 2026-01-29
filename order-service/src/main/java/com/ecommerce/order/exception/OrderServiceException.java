package com.ecommerce.order.exception;

import com.ecommerce.common.exception.BaseServiceException;

public class OrderServiceException extends BaseServiceException {
    public OrderServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public OrderServiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}

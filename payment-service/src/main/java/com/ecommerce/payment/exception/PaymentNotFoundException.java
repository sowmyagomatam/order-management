package com.ecommerce.payment.exception;

import com.ecommerce.common.exception.BaseServiceException;

public class PaymentNotFoundException extends BaseServiceException {

    public PaymentNotFoundException(String orderId){
        super(String.format("Payment not found for Order id : %s", orderId),
                "NOT_FOUND");
    }
}

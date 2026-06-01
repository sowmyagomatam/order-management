package com.ecommerce.payment.domain;

public enum PaymentStatus {
    PENDING,       // Payment created, not yet processed
    PROCESSING,    // Payment being processed
    COMPLETED,     // Payment successful
    FAILED,        // Payment failed
    REFUNDED       // Payment refunded
    ;

    public boolean canTransitionTo(PaymentStatus newPaymentStatus) {
       return  switch(this){
            case PENDING -> newPaymentStatus == PROCESSING ||
                    newPaymentStatus == COMPLETED ||
                    newPaymentStatus == FAILED;
            case PROCESSING -> newPaymentStatus == COMPLETED ||
                    newPaymentStatus == FAILED;
           case COMPLETED -> newPaymentStatus == REFUNDED;
           case FAILED, REFUNDED -> false;
        };
    }
}

package com.ecommerce.order.domain;

public enum CancellationReason {
    OUT_OF_STOCK,           // Insufficient inventory
    CUSTOMER_REQUEST,       // Customer cancelled
    PAYMENT_FAILED,         // Payment processing failed
    FRAUD_DETECTED,         // Fraud check failed (Week 3!)
    SYSTEM_ERROR,           // Technical error
    TIMEOUT                 // Order expired/timeout
}

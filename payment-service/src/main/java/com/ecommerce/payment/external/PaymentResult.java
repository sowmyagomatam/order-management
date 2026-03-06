package com.ecommerce.payment.external;

public record PaymentResult(
        boolean success,
        String paymentReference,  // Gateway transaction ID (if success)
        String failureReason      // Reason (if failed)
) {
    public static PaymentResult success(String paymentReference) {
        return new PaymentResult(true, paymentReference, null);
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
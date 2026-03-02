package com.ecommerce.payment.domain;

import com.ecommerce.payment.exception.InvalidPaymentStateException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Payment {
    @Id
    @Column(name = "payment_id")
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "card_last_four_digits", length = 4)
    private String cardLastFourDigits;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void beforeSave() {
        if (id == null){
            id = "PAY-" + UUID.randomUUID().toString();
        }
        if(createdAt == null){
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Mark payment as completed
     * @param paymentReference
     */
    public void markAsCompleted(String paymentReference) {
        if(!canTransitionTo(PaymentStatus.COMPLETED)){
            throw new InvalidPaymentStateException(
                    this.id,
                    this.paymentStatus,
                    PaymentStatus.COMPLETED
            );
        }
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new IllegalArgumentException("Payment reference cannot be null or blank");
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paymentReference = paymentReference;
    }

    public void markAsFailed(String failureReason){
        if(!canTransitionTo(PaymentStatus.FAILED)){
            throw new InvalidPaymentStateException(
                    this.id,
                    this.paymentStatus,
                    PaymentStatus.FAILED
            );
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("Failure reason cannot be null or blank");
        }
        this.failureReason = failureReason;
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void markAsProcessing(){
        if(!canTransitionTo(PaymentStatus.PROCESSING)){
            throw new InvalidPaymentStateException(
                    this.id,
                    this.paymentStatus,
                    PaymentStatus.PROCESSING
            );
        }

        this.paymentStatus = PaymentStatus.PROCESSING;
    }


    private boolean canTransitionTo(PaymentStatus paymentStatus) {
        return this.paymentStatus.canTransitionTo(paymentStatus);
    }

}

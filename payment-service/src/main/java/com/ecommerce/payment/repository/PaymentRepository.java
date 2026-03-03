package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    /**
     * Find payment by order ID
     * Assumes one payment per order
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Check if payment exists for order with specific status
     * Useful for idempotency checks
     */
    boolean existsByOrderIdAndPaymentStatus(String orderId, PaymentStatus status);
}

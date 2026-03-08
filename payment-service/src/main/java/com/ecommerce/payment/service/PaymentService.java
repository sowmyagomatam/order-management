package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.domain.command.ProcessPaymentCommand;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentService {
    /**
     * Create payment for an order
     * Idempotent: Returns existing payment if already exists
     *
     * @param createPaymentCommand
     * @return Created or existing payment
     */
    Payment createPayment(CreatePaymentCommand createPaymentCommand);

    /**
     * Process payment through gateway and update status
     *
     * @param processPaymentCommand Payment to process
     * @return Updated payment with new status
     */
    Payment processPayment(ProcessPaymentCommand processPaymentCommand);

    /**
     * Get payment by order ID
     *
     * @param orderId Order ID
     * @return Payment if found
     * @throws PaymentNotFoundException if not found
     */
   Optional<Payment> findByOrderId(String orderId);

}

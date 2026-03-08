package com.ecommerce.payment.controller;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.dto.response.PaymentResponse;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping()
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody  PaymentRequest paymentRequest) {
        log.info("Received payment creation request for order: {}", paymentRequest.orderId());
        Payment payment = paymentService.createPayment(CreatePaymentCommand.builder()
                        .orderId(paymentRequest.orderId())
                        .amount(paymentRequest.amount())
                        .paymentMethod(paymentRequest.paymentMethod())
                        .cardLastFourDigits(paymentRequest.cardLastFourDigits())
                .build());


        log.info("Payment created: {} for order: {}", payment.getId(), payment.getOrderId());
        return ResponseEntity.created(URI.create("/api/payments/" + payment.getId()))
                .body(paymentMapper.toPaymentResponse(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> findPaymentByOrderId(@PathVariable String orderId){
        log.info("Fetching payment for order: {}", orderId);
        PaymentResponse paymentResponse = paymentService.findByOrderId(orderId)
                .map(paymentMapper::toPaymentResponse)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));

        return ResponseEntity.ok(paymentResponse);

    }
}

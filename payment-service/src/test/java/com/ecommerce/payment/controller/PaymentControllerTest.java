package com.ecommerce.payment.controller;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.mapper.PaymentMapperImpl;
import com.ecommerce.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(PaymentController.class)
@Import({PaymentMapperImpl.class})
public class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId("ORDER-123")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .amount(BigDecimal.ONE)
                .cardLastFourDigits("1234")
                .build();

        Payment mockResponse = Payment.builder()
                .id("PAYMENT-123")
                .orderId("ORDER-123")
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("1.00"))
                .createdAt(Instant.now())
                .build();

        when(paymentService.createPayment(any(CreatePaymentCommand.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/payments/PAYMENT-123"))
                .andExpect(jsonPath("$.id").value("PAYMENT-123"))
                .andExpect(jsonPath("$.orderId").value("ORDER-123"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(1.00));

        ArgumentCaptor<CreatePaymentCommand> commandCaptor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
        verify(paymentService).createPayment(commandCaptor.capture());
        CreatePaymentCommand command = commandCaptor.getValue();
        assertThat(command.orderId()).isEqualTo("ORDER-123");
        assertThat(command.amount()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(command.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(command.cardLastFourDigits()).isEqualTo("1234");

    }

    @Test
    void shouldFindPaymentByOrderId() throws Exception {

        Payment mockResponse = Payment.builder()
                .id("PAYMENT-123")
                .orderId("ORDER-123")
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("1.00"))
                .createdAt(Instant.now())
                .build();

        when(paymentService.findByOrderId("ORDER-123"))
                .thenReturn(Optional.of(mockResponse));

        mockMvc.perform(get("/api/payments/order/{orderId}", "ORDER-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PAYMENT-123"))
                .andExpect(jsonPath("$.orderId").value("ORDER-123"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(1.00));

    }

    @Test
    void shouldThrowExceptionWhenOrderIdNotFound() throws Exception {

        String orderId = "ORDER-123";

        when(paymentService.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/order/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Payment not found for Order id : " + orderId));

    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        PaymentRequest invalidRequest = PaymentRequest.builder()
                .orderId("")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .amount(BigDecimal.ONE)
                .cardLastFourDigits("1234")
                .build();

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("orderId:Order Id is required"));

    }
}


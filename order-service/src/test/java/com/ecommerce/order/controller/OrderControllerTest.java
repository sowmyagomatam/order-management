package com.ecommerce.order.controller;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.request.AddressRequest;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        OrderRequest orderRequest = OrderRequest.builder()
                .customerId("CUST-001")
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId("PROD-1")
                                .productSku("SKU-1")
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()
                ))
                .shippingAddress(AddressRequest.builder()
                        .street("street-123")
                        .city("city-123")
                        .state("state-123")
                        .country("country-123")
                        .zipCode("zipCode-123")
                        .build())
                .build();

        OrderResponse mockResponse = OrderResponse.builder()
                .id("ORDER-123")
                .customerId("CUST-001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .itemCount(1)
                .totalItemQuantity(2)
                .createdAt(Instant.now())
                .build();

        // Mock service
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/ORDER-123"))
                .andExpect(jsonPath("$.id").value("ORDER-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void shouldGetOrderById() throws Exception {

        OrderResponse mockResponse = OrderResponse.builder()
                .id("ORDER-123")
                .customerId("CUST-001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .itemCount(1)
                .totalItemQuantity(2)
                .createdAt(Instant.now())
                .build();

        // Mock service
        when(orderService.getOrder(eq("ORDER-123")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/orders/{id}", "ORDER-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ORDER-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void shouldGetAllOrders() throws Exception {

        OrderResponse mockResponse = OrderResponse.builder()
                .id("ORDER-123")
                .customerId("CUST-001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .itemCount(1)
                .totalItemQuantity(2)
                .createdAt(Instant.now())
                .build();

        // Mock service
        when(orderService.getAllOrders())
                .thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("ORDER-123"));
    }

    @Test
    void shouldReturnErrorWhenOrderNotFound() throws Exception {
        // Given
        String orderId = "NON-EXISTENT";
        when(orderService.getOrder(orderId))
                .thenThrow(new OrderNotFoundException(orderId));

        // When/Then
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Order not found with Id: " + orderId));
    }

    @Test
    void shouldReturnErrorWhenValidationFails() throws Exception {
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerId("")  // Invalid - blank
                .items(List.of())  // Invalid - empty list
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").exists());
    }

}

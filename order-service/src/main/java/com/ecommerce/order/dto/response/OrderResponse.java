package com.ecommerce.order.dto.response;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderResponse {
    private String id;
    private String customerId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private Integer totalItemQuantity;
    private AddressResponse shippingAddress;
    private AddressResponse billingAddress;
    private Instant createdAt;
    private Instant updatedAt;
}

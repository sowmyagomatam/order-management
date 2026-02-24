package com.ecommerce.order.dto.response;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.CancellationReason;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String customerId;
    private OrderStatus status;
    private CancellationReason cancellationReason;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private Integer totalItemQuantity;
    private AddressResponse shippingAddress;
    private AddressResponse billingAddress;
    private Instant createdAt;
    private Instant updatedAt;
}

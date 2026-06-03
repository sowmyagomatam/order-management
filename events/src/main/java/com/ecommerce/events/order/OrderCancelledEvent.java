package com.ecommerce.events.order;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Order cancelled event published whenever an order is cancelled.
 * It is consumed by the Inventory service to release stock for instance.
 */
@Builder
public record OrderCancelledEvent(String orderId,
                                  String reason,
                                  List<OrderItemEvent> items,
                                  Instant timestamp) {

    public OrderCancelledEvent{
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

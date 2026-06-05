package com.ecommerce.events.order;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Order confirmed event published when an order is fulfilled (payment completed).
 * Consumed by the Inventory service to confirm reservations, drawing the held stock
 * down from reserved quantity.
 */
@Builder
public record OrderConfirmedEvent(String orderId,
                                  List<OrderItemEvent> items,
                                  Instant timestamp) {

    public OrderConfirmedEvent{
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

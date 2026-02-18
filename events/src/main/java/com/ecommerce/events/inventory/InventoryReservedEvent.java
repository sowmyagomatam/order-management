package com.ecommerce.events.inventory;

import java.time.Instant;
import java.util.List;

public record InventoryReservedEvent(String orderId,
                                     List<ReservedItemEvent> reservedItems,
                                     Instant timestamp) {

    public InventoryReservedEvent {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (reservedItems == null || reservedItems.isEmpty()) {
            throw new IllegalArgumentException("Reserved items cannot be empty");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

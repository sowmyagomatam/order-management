package com.ecommerce.events.inventory;

import java.time.Instant;
import java.util.List;

public record InventoryReservationFailedEvent(
        String orderId,
        List<FailedItemEvent> failedItems,
        String reason,
        Instant timestamp
) {
    public InventoryReservationFailedEvent {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (failedItems == null || failedItems.isEmpty()) {
            throw new IllegalArgumentException("Failed items cannot be empty");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

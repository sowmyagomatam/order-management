package com.ecommerce.inventory.dto;

import com.ecommerce.events.inventory.FailedItemEvent;
import com.ecommerce.events.inventory.ReservedItemEvent;

import java.util.List;

/**
 * Outcome of an all-or-nothing reservation attempt for an order.
 * Exactly one list is non-empty: reservedItems on full success, failedItems otherwise.
 */
public record ReservationResult(List<ReservedItemEvent> reservedItems,
                                List<FailedItemEvent> failedItems) {
}

package com.ecommerce.events.inventory;

public record FailedItemEvent( String productId,
                               String productSku,
                               Integer requestedQuantity,
                               Integer availableQuantity,
                               String reason) {
}

package com.ecommerce.events.inventory;

public record ReservedItemEvent(   String productId,
                                   String productSku,
                                   Integer quantityReserved) {
}

package com.ecommerce.events.inventory;

import java.math.BigDecimal;

public record ReservedItemEvent(String productId,
                                String productSku,
                                BigDecimal price,
                                Integer quantityReserved) {
}

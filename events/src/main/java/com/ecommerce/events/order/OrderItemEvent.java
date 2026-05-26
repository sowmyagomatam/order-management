package com.ecommerce.events.order;

import java.math.BigDecimal;

/**
 * Represents an order item in payment events
 * Used for inventory compensation when payment fails
 */
public record OrderItemEvent(String productId,
                             String productSku,
                             String productName,
                             Integer quantity,
                             BigDecimal unitPrice) {
}

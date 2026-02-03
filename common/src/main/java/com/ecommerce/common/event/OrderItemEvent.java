package com.ecommerce.common.event;

import java.math.BigDecimal;

public record OrderItemEvent(String productId,
                             String productSku,
                             String productName,
                             Integer quantity,
                             BigDecimal unitPrice) {
}

package com.ecommerce.inventory.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String productId;
    private String productName;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;  // available + reserved
    private boolean inStock;  // availableQuantity > 0
    Instant createdAt;
    Instant updatedAt;
}

package com.ecommerce.order.dto.request;

import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    @NotBlank(message =  "Product Id is required")
    private String productId;

    @NotBlank(message = "Product Name is required")
    private String productName;

    @NotBlank(message = "Product SKU is required")
    private String productSku;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be atleast 1")
    private Integer quantity;

    @NotNull(message =  "Product Id is required")
    @Min(value = 0, message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private String productMetadata;

}

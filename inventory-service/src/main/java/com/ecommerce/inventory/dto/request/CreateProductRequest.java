package com.ecommerce.inventory.dto.request;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductRequest {
    @NotBlank(message = "Product name is mandatory")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;


    @NotNull(message = "Initial quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer initialQuantity;
}

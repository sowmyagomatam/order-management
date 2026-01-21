package com.ecommerce.inventory.domain;

import com.ecommerce.inventory.exception.InsufficientStockException;
import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "product_id", nullable = false)
    private String productId;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "product_sku", nullable = false)
    private String sku;
    private String description;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(name = "available_quantity")
    @Builder.Default
    private Integer availableQuantity = 0;
    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;
    @Column(nullable = false, updatable = false)
    Instant createdAt;
    @Column(nullable = false)
    Instant updatedAt;

    @PrePersist
    private void beforeSave(){
        if (productId == null){
            productId = UUID.randomUUID().toString();
        }
        if(createdAt == null){
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void beforeUpdate(){
        this.updatedAt = Instant.now();
    }

    public void reserveStock(Integer quantity){
        Preconditions.checkArgument(quantity != null && quantity > 0,"Quantity must be positive");

        if(quantity > availableQuantity){
            throw new InsufficientStockException(this.productId, quantity, availableQuantity);
        }

        reservedQuantity += quantity;
        availableQuantity -= quantity;
    }

    public void releaseStock(Integer quantity){
        Preconditions.checkArgument(quantity != null && quantity > 0,"Quantity must be positive");

        if(quantity > reservedQuantity){
            throw new IllegalStateException(
                    String.format("Cannot release %d units, only %d reserved",
                            quantity, reservedQuantity)
            );
        }

        reservedQuantity -= quantity;
        availableQuantity += quantity;

    }

    /**
     * When product sold, release from reserved quantity
     * @param quantity
     */
    public void confirmReservation(Integer quantity) {
        Preconditions.checkArgument(quantity != null && quantity > 0,"Quantity must be positive");

        if(quantity > reservedQuantity){
            throw new IllegalStateException(
                    String.format("Cannot confirm %d units, only %d reserved",
                            quantity, reservedQuantity)
            );
        }

        reservedQuantity -= quantity;
    }

    public boolean isStockAvailable(Integer quantity) {
        return availableQuantity >= quantity;
    }
}

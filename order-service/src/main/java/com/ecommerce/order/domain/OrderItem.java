package com.ecommerce.order.domain;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="order_items")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents a single line of item in an Order
 *
 */
//OrderItem is mutable here but controlled via it's aggregate 'Order'
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Reference to the product for queries
    @Column(nullable = false)
    private String productId;
    //Snapshot of product at purchase time
    @Column(nullable = false)
    private String productName;
    //Snapshot of product at purchase time
    @Column(nullable = false)
    private String productSku;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // additional product data, that we can parse while publishing to Kafka say
    @Column(columnDefinition = "TEXT")
    private String productMetadata;

    @PrePersist
    @PreUpdate
    private void calculateSubtotal() {
        this.subtotal =  getSubtotal();
    }

    public BigDecimal getSubtotal() {
        if (this.subtotal == null) {
            return calculateSubtotalValue();
        }
        return subtotal;
    }

    private BigDecimal calculateSubtotalValue() {
        if (unitPrice != null && quantity != null && quantity > 0) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public void validate(){
        Preconditions.checkArgument(productId != null && !productId.isEmpty(), "Product id is required");
        Preconditions.checkArgument(quantity != null && quantity > 0,
               "quantity must be positive" );
        Preconditions.checkArgument(unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0,
                "Item price must be positive");

    }

}

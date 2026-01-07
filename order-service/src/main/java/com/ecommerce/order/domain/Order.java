package com.ecommerce.order.domain;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name="order_status", nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_address")),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))

    })
    private Address shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "billing_address")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip_code")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country"))

    })
    private Address billingAddress;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void beforeSave() {
        if (id == null){
            id = UUID.randomUUID().toString();
        }
        if(status == null){
            this.status = OrderStatus.PENDING;
        }
        if(createdAt == null){
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();

    }

    @PreUpdate
    private void  beforeUpdate(){
        updatedAt = Instant.now();
    }

    public void validate(){
        Preconditions.checkArgument(items != null && !items.isEmpty(),
                "Order must have at least one item");
        Preconditions.checkArgument(totalAmount != null &&
                totalAmount.compareTo(BigDecimal.ZERO) >= 0, "Amount should be positive");
        Preconditions.checkArgument(customerId != null && !customerId.isBlank(), "Customer id should be present");
        Preconditions.checkArgument(shippingAddress != null , "Shipping address must be present and valid");
        shippingAddress.validate();
    }

    public void addItem(OrderItem orderItem){
        if(orderItem == null){
            throw new IllegalArgumentException("Order item cannot be null");
        }
        orderItem.validate();
        if(status != OrderStatus.PENDING){
            throw new IllegalStateException("Cannot add an item when status = " + status);
        }

        this.items.add(orderItem);
        reCalculateTotal();
    }

    public void removeItem(OrderItem orderItem){
        if(status != OrderStatus.PENDING){
            throw new IllegalStateException("Cannot remove an item when status = " + status);
        }
        orderItem.validate();
        this.items.remove(orderItem);
        reCalculateTotal();
    }

    public void updateStatus(OrderStatus newStatus){
        if(this.status.canTransitionTo(newStatus)){
            this.status = newStatus;
        } else {
            throw new IllegalStateException("Cannot update status to " + newStatus  + " from " + status);
        }
        this.updatedAt = Instant.now();
    }

    public void cancel(){
        if(!status.isCancellable()){
            throw new IllegalStateException("Cannot cancel order in status " + status);
        } else {
            this.status = OrderStatus.CANCELLED;
        }
    }

    public Integer getItemCount(){
        return this.items.size();
    }

    public boolean hasItem(String productId){
        return this.items.stream()
                .anyMatch(orderItem -> orderItem.getProductId().equals(productId));
    }

    public Integer getTotalItemQuantity(){
        return this.items.stream()
                .map(OrderItem::getQuantity)
                .reduce(0, Integer::sum);
    }

    public void reCalculateTotal(){
       this.totalAmount =  this.items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

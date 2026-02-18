package com.ecommerce.inventory.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends InventoryServiceException {

    private int requested;
    private int available;

    public InsufficientStockException(String productId, Integer requested, Integer available) {
        super(
                String.format("Insufficient stock for product %s: requested %d, available %d",
                        productId, requested, available),
                "INSUFFICIENT_STOCK"
        );
        this.requested = requested;
        this.available = available;
    }
}

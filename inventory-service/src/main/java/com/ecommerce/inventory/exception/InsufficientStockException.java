package com.ecommerce.inventory.exception;

public class InsufficientStockException extends InventoryServiceException {

    public InsufficientStockException(String productId, Integer requested, Integer available) {
        super(
                String.format("Insufficient stock for product %s: requested %d, available %d",
                        productId, requested, available),
                "INSUFFICIENT_STOCK"
        );
    }
}

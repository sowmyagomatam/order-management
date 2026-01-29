package com.ecommerce.inventory.exception;

public class ProductNotFoundException extends InventoryServiceException {

    public ProductNotFoundException(String productId) {
        super(
                String.format("Product not found with id: %s", productId),
                "PRODUCT_NOT_FOUND"
        );
    }
}

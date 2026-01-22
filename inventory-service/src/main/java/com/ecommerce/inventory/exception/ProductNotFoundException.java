package com.ecommerce.inventory.exception;

public class ProductNotFoundException extends InventoryServiceException {

    public ProductNotFoundException(String productId) {
        super(
                String.format("Product %s not found", productId),
                "PRODUCT_NOT_FOUND"
        );
    }
}

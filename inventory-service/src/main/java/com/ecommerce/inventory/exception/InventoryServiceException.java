package com.ecommerce.inventory.exception;

public class InventoryServiceException extends RuntimeException{
    private final String errorCode;

    public InventoryServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InventoryServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}

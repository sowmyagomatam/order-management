package com.ecommerce.inventory.exception;

import com.ecommerce.common.exception.BaseServiceException;

public class InventoryServiceException extends BaseServiceException {

    public InventoryServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

}

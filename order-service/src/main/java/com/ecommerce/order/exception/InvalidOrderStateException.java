package com.ecommerce.order.exception;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;

public class InvalidOrderStateException extends OrderServiceException {
    public InvalidOrderStateException(String orderId, OrderStatus status, OrderStatus newStatus) {
        super(
                String.format("Cannot transition order %s from %s to %s",
                        orderId, status, newStatus),
                "INVALID_ORDER"
        );
    }
}

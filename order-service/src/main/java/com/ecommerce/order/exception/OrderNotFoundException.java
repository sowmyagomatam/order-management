package com.ecommerce.order.exception;

public class OrderNotFoundException extends RuntimeException  {
    public OrderNotFoundException(String orderId) {
        super("Order not found with Id {}" + orderId);
    }

    public OrderNotFoundException(String orderId, Throwable cause) {
        super("Order not found with Id {}" + orderId, cause);
    }
}

package com.ecommerce.order.exception;

public class OrderNotFoundException extends OrderServiceException  {
    public OrderNotFoundException(String orderId) {
        super("Order not found with Id: " + orderId,
                "ORDER_NOT_FOUND");
    }
}

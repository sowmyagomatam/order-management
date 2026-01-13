package com.ecommerce.order.utils;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public class OrderUtils {

    public static Order createTestOrder(String customerId, List<OrderItem> items) {
       return createTestOrder(customerId, items, createAddress());
    }

    public static Order createTestOrder(String customerId, List<OrderItem> items, Address shippingAddress) {
        Order order = Order.builder()
                .customerId(customerId)
                .shippingAddress(shippingAddress)
                .billingAddress(shippingAddress)
                .status(OrderStatus.PENDING)
                //  .items(items)
                .build();
        items.forEach(order::addItem);
        return order;
    }

    public static OrderItem createOrderItem(String productId, int quantity, String unitPrice) {
        return OrderItem.builder()
                .productId(productId)
                .productName("Product " + productId)
                .productSku(productId + "-SKU")
                .quantity(quantity)
                .unitPrice(new BigDecimal(unitPrice))
                .build();
    }

    public static Address createAddress() {
        return Address.builder()
                .street("5 Main St")
                .city("London")
                .state("London")
                .zipCode("E1 7DS")
                .country("GB")
                .build();
    }
}

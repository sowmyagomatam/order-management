package com.ecommerce.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderItemTest {

    @Test
    void validateProductId(){
        OrderItem order = OrderItem.builder()
                .id(1L)
                .build();
        assertThrows(IllegalArgumentException.class, order::validate);
    }

    @Test
    void validatePrice(){
        OrderItem order = OrderItem.builder()
                .id(1L)
                .productId("1")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(-1))
                .build();
        assertThrows(IllegalArgumentException.class, order::validate);
    }

    @Test
    void validateQuantity(){
        OrderItem order = OrderItem.builder()
                .id(1L)
                .productId("1")
                .quantity(-1)
                .build();
        assertThrows(IllegalArgumentException.class, order::validate);
    }
}

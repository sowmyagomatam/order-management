package com.ecommerce.order.dto.response;

import jakarta.persistence.Column;

import java.math.BigDecimal;

public class OrderItemResponse {
    private String id;
    private String productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String productMetadata;
}

package com.ecommerce.order.mapper;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.dto.request.AddressRequest;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderMapperTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldMapRequestToOrderEntity() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .customerId("customer-123")
                .shippingAddress(createAddressRequest())
                .billingAddress(createAddressRequest())
                .build();

        // When
        Order order = orderMapper.toEntity(request);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-123");
        assertThat(order.getShippingAddress()).isNotNull();
        assertThat(order.getBillingAddress()).isNotNull();

        // These should be null (handled separately)
        assertThat(order.getId()).isNull();
        assertThat(order.getItems()).isEmpty();  // Empty list from @Builder.Default
        assertThat(order.getTotalAmount()).isNull();
    }

    @Test
    void shouldMapOrderToResponse() {
        // Given
        Order order = Order.builder()
                .customerId("customer-123")
                .shippingAddress(createAddress())
                .billingAddress(createAddress())
                .build();

        // Add an item (triggers total calculation)
        OrderItem item = OrderItem.builder()
                .productId("prod-1")
                .productName("Test Product")
                .productSku("TEST-SKU")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        order.addItem(item);

        // When
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo("customer-123");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getItemCount()).isEqualTo(1);
        assertThat(response.getTotalItemQuantity()).isEqualTo(2);
        assertThat(response.getItems()).hasSize(1);
    }

    // Helper methods
    private AddressRequest createAddressRequest() {
        return AddressRequest.builder()
                .street("123 Main St")
                .city("London")
                .state("London")
                .zipCode("E1 7DS")
                .country("GB")
                .build();
    }

    private Address createAddress() {
        return Address.builder()
                .street("123 Main St")
                .city("London")
                .state("London")
                .zipCode("E1 7DS")
                .country("GB")
                .build();
    }
}

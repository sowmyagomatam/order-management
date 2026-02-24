package com.ecommerce.order.domain;

import com.ecommerce.order.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.ecommerce.order.domain.OrderStatus.*;
import static com.ecommerce.order.utils.OrderUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

public class OrderTest {

    @Test
    void shouldInitializeWithPendingStatus(){
        Order order = createTestOrder("customer123", new ArrayList<>());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }


    @Test
    void shouldValidateOrderWithValidData(){
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "10.00")));
        assertThatCode(order::validate).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void shouldValidateOrderWithInvalidData(Order order, String expectedMessage){
        assertThatThrownBy(order::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> invalidOrders() {
        return Stream.of(
                Arguments.of(createTestOrder("customer123", List.of()),
                "Order must have at least one item"
                ),
                Arguments.of(createTestOrder("",
                               List.of(createOrderItem("1", 1,"10.00"))),
                        "Customer id should be present"
                ),
                Arguments.of(createTestOrder("customer123",
                                List.of(createOrderItem("1", 1,"10.00")),
                                null),
                        "Shipping address must be present and valid"
                ));
    }
    @Test
    void shouldRecalculateTotalAfterAddingItems(){
        Order order = createTestOrder("customer123", new ArrayList<>());
        order.addItem(createOrderItem("prod-1", 1, "10.00"));
        order.addItem(createOrderItem("prod-2", 2, "20.00"));
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));

    }

    @Test
    void shouldNotAllowAddingItemsWhenNotPending(){
        Order order = Order.builder()
                .customerId("customer123")
                .shippingAddress(createAddress())
                .billingAddress(createAddress())
                .status(OrderStatus.CONFIRMED)
                .build();

        assertThatThrownBy(() -> order.addItem(createOrderItem("prod-1", 1, "10.00"))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRecalculateTotalAfterRemovingItems() {
        OrderItem orderItem1 = createOrderItem("prod-1", 1, "10.00");
        OrderItem orderItem2 = createOrderItem("prod-2", 1, "10.00");
        Order order = Order.builder()
                .customerId("customer123")
                .shippingAddress(createAddress())
                .billingAddress(createAddress())
                .status(OrderStatus.PENDING)
                .build();
        order.addItem(orderItem1);
        order.addItem(orderItem2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        order.removeItem(orderItem1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void shouldNotAllowRemovingItemsWhenNotPending(){

        OrderItem orderItem = createOrderItem("prod-1", 1, "10.00");
        Order order = Order.builder()
                .customerId("customer123")
                .shippingAddress(createAddress())
                .billingAddress(createAddress())
                .status(OrderStatus.CONFIRMED)
                .items(List.of(orderItem))
                .build();
        assertThatThrownBy(() -> order.removeItem(orderItem))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCalculateZeroTotalWhenAllItemsRemoved(){
        OrderItem orderItem1 = createOrderItem("prod-1", 1, "10.00");
        Order order = Order.builder()
                .customerId("customer123")
                .shippingAddress(createAddress())
                .billingAddress(createAddress())
                .status(OrderStatus.PENDING)
                .build();
        order.addItem(orderItem1);
        order.removeItem(orderItem1);
        assertThat(order.getItems().isEmpty()).isTrue();
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

    }

    @Test
    void shouldAllowStatusUpdateForValidTransitions(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        assertThatCode( () -> {
            order.updateStatus(INVENTORY_RESERVED);
            order.updateStatus(PAYMENT_PROCESSING);
            order.updateStatus(PAYMENT_COMPLETED);
            order.updateStatus(CONFIRMED);
            order.updateStatus(SHIPPED);
            order.updateStatus(DELIVERED);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldNotAllowStatusUpdateForIllegalTransitions(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        order.updateStatus(INVENTORY_RESERVED);
        assertThatThrownBy( () ->
            order.updateStatus(SHIPPED)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldUpdateTimestampWhenStatusUpdated(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        order.updateStatus(INVENTORY_RESERVED);
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldAllowCancellationForValidTransitions(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        order.updateStatus(INVENTORY_RESERVED);
        order.cancel(CancellationReason.CUSTOMER_REQUEST, "User");
        assertThat(order.getStatus()).isEqualTo(CANCELLED);
    }

    @Test
    void shouldNotAllowCancellationForInValidTransitions(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        order.updateStatus(INVENTORY_RESERVED);
        order.updateStatus(PAYMENT_PROCESSING);
        order.updateStatus(PAYMENT_COMPLETED);
        order.updateStatus(CONFIRMED);
        order.updateStatus(SHIPPED);
        assertThatThrownBy(() -> order.cancel(CancellationReason.CUSTOMER_REQUEST, "user"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCheckOrderHasSpecificProduct(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00")));
        assertThat(order.hasItem("prod-1")).isTrue();
    }

    @Test
    void shouldGetCorrectItemCount(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00"),
                        createOrderItem("prod-2", 1, "10.00")));
        assertThat(order.getItemCount()).isEqualTo(2);
    }

    @Test
    void shouldGetCorrectItemQuantity(){
        Order order = createTestOrder("customer123",
                List.of( createOrderItem("prod-1", 1, "10.00"),
                        createOrderItem("prod-2", 3, "10.00")));
        assertThat(order.getTotalItemQuantity()).isEqualTo(4);
    }


}

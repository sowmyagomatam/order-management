package com.ecommerce.order.messaging;

import com.ecommerce.events.inventory.FailedItemEvent;
import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.inventory.ReservedItemEvent;
import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.order.domain.CancellationReason;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.mapper.OrderItemEventMapper;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.ecommerce.order.utils.OrderUtils.createOrderItem;
import static com.ecommerce.order.utils.OrderUtils.createTestOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the compensation behaviour added to {@link InventoryEventConsumer}:
 * publishing orders.cancelled (OUT_OF_STOCK) when inventory reservation fails.
 */
@ExtendWith(MockitoExtension.class)
class InventoryEventConsumerTest {

    private static final String ORDER_ID = "ORDER-1";

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderEventProducer orderEventProducer;
    @Mock
    OrderItemEventMapper orderItemEventMapper;
    @Mock
    Acknowledgment acknowledgment;

    @InjectMocks
    InventoryEventConsumer consumer;

    private static Order orderWithStatus(OrderStatus status) {
        Order order = createTestOrder("customer-1",
                List.of(createOrderItem("PROD-1", 2, "50.00")));
        order.setId(ORDER_ID);
        order.setStatus(status);
        return order;
    }

    @Test
    void handleInventoryReservationFailedShouldCancelOrder() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemEventMapper.toOrderItemEventList(any())).thenReturn(List.of());

        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(
                ORDER_ID,
                List.of(new FailedItemEvent("PROD-1", "PROD-1-SKU", 10, 3, "Insufficient stock")),
                "1 of 1 items failed reservation",
                Instant.now());

        consumer.handleInventoryReservationFailed(event, 0, 0L, acknowledgment);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(CancellationReason.OUT_OF_STOCK);
        verify(orderRepository).save(order);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(orderEventProducer).publishOrderCancelledEvent(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().reason()).isEqualTo(CancellationReason.OUT_OF_STOCK.name());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleInventoryReservedShouldUpdateStatusAndDoesNotPublishCancellation() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        InventoryReservedEvent event = new InventoryReservedEvent(
                ORDER_ID,
                List.of(new ReservedItemEvent("PROD-1", "PROD-1-SKU", new BigDecimal("50.00"), 2)),
                Instant.now());

        consumer.handleInventoryReserved(event, 0, 0L, acknowledgment);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        verify(orderEventProducer, never()).publishOrderCancelledEvent(any());
        verify(acknowledgment).acknowledge();
    }
}

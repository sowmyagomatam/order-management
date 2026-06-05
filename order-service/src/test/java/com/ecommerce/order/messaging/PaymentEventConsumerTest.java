package com.ecommerce.order.messaging;

import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.events.order.OrderConfirmedEvent;
import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
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
 * Unit tests for the compensation/confirmation behaviour added to
 * {@link PaymentEventConsumer}: publishing orders.confirmed on payment success
 * and orders.cancelled on payment failure.
 */
@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

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
    PaymentEventConsumer consumer;

    private static Order orderWithStatus(OrderStatus status) {
        Order order = createTestOrder("customer-1",
                List.of(createOrderItem("PROD-1", 2, "50.00")));
        order.setId(ORDER_ID);
        order.setStatus(status);
        return order;
    }

    @Test
    void paymentCompletedShouldConfirmsOrderAndPublishOrderConfirmedEvent() {
        Order order = orderWithStatus(OrderStatus.PAYMENT_PROCESSING);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemEventMapper.toOrderItemEventList(any())).thenReturn(List.of());

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                "PAY-1", ORDER_ID, new BigDecimal("100.00"), "CARD", "REF-1", Instant.now());

        consumer.handlePaymentCompleted(event, 0, 0L, acknowledgment);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order);

        ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(orderEventProducer).publishOrderConfirmedEvent(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(ORDER_ID);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void paymentCompletedWhenorderNotFoundShouldNotPublishButAcknowledge() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                "PAY-1", ORDER_ID, new BigDecimal("100.00"), "CARD", "REF-1", Instant.now());

        consumer.handlePaymentCompleted(event, 0, 0L, acknowledgment);

        verify(orderEventProducer, never()).publishOrderConfirmedEvent(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void paymentFailedShouldCancelOrder() {
        Order order = orderWithStatus(OrderStatus.PAYMENT_PROCESSING);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemEventMapper.toOrderItemEventList(any())).thenReturn(List.of());

        PaymentFailedEvent event = new PaymentFailedEvent(
                "PAY-1", ORDER_ID, new BigDecimal("100.00"), "Card declined", List.of(), Instant.now());

        consumer.handlePaymentFailed(event, 0, 0L, acknowledgment);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(CancellationReason.PAYMENT_FAILED);
        verify(orderRepository).save(order);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(orderEventProducer).publishOrderCancelledEvent(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().reason()).isEqualTo(CancellationReason.PAYMENT_FAILED.name());
        verify(acknowledgment).acknowledge();
    }
}

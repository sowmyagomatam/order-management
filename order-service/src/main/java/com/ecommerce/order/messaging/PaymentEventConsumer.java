package com.ecommerce.order.messaging;

import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.events.order.OrderConfirmedEvent;
import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
import com.ecommerce.events.payment.PaymentProcessingEvent;
import com.ecommerce.order.domain.CancellationReason;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.mapper.OrderItemEventMapper;
import com.ecommerce.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final OrderItemEventMapper orderItemEventMapper;

    @KafkaListener(
            topics = "payment.processing",
            groupId = "order-service",
            containerFactory = "paymentProcessingListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentProcessing(
            @Payload PaymentProcessingEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received PaymentProcessing for order: {} from partition: {} offset: {}",
                    event.orderId(), partition, offset);

            orderRepository.findById(event.orderId()).ifPresentOrElse(
                    order -> {
                        order.updateStatus(OrderStatus.PAYMENT_PROCESSING);
                        orderRepository.save(order);
                        log.info("Order {} status updated to PAYMENT_PROCESSING", order.getId());
                    },
                    () -> log.warn("Order {} not found", event.orderId())
            );

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("Error processing PaymentProcessingEvent for order: {}", event.orderId(), e);
        }
    }

    /**
     * Handles payment completed
     */
    @KafkaListener(
            topics = "payment.completed",
            groupId = "order-service",
            containerFactory = "paymentCompletedEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCompleted(
            @Payload PaymentCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment){

        try {
            log.info("Received PaymentCompleted for order: {} from partition: {} offset: {}",
                    event.orderId(), partition, offset);

            orderRepository.findById(event.orderId()).ifPresentOrElse(
                    order -> {
                        order.updateStatus(OrderStatus.PAYMENT_COMPLETED);
                        order.updateStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                        //publish order confirmed event so inventory can confirm the reservation
                        orderEventProducer.publishOrderConfirmedEvent(OrderConfirmedEvent.builder()
                                .orderId(event.orderId())
                                .items(orderItemEventMapper.toOrderItemEventList(order.getItems()))
                                .build());
                        log.info("Order {} confirmed", order.getId());
                    },
                    () -> log.warn("Order {} not found", event.orderId())
            );

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged PaymentCompleted for order: {}", event.orderId());
            }

        }catch (Exception e) {
            log.error("Error processing PaymentCompleted for order: {}",
                    event.orderId(), e);
            // Don't acknowledge - will retry
        }


    }

    /**
     * Handles payment failed
     */
    @KafkaListener(
            topics = "payment.failed",
            groupId = "order-service",
            containerFactory = "paymentFailedEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(
            @Payload PaymentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {


            log.info("Received PaymentFailed for order: {} from partition: {} offset: {}",
                    event.orderId(), partition, offset);

            orderRepository.findById(event.orderId()).ifPresentOrElse(
                    order -> {
                        order.cancel(CancellationReason.PAYMENT_FAILED, "System");
                        orderRepository.save(order);
                        //publish order cancelled event since the payment failed
                        orderEventProducer.publishOrderCancelledEvent(OrderCancelledEvent.builder()
                                .orderId(event.orderId())
                                .items(orderItemEventMapper.toOrderItemEventList(order.getItems()))
                                 .reason(CancellationReason.PAYMENT_FAILED.name())
                                .build());
                        log.info("Order {} cancelled (payment failed)", order.getId());
                    },
                    () -> log.warn("Order {} not found", event.orderId())
            );


            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged PaymentFailed for order: {}", event.orderId());
            }

        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent for order: {}",
                    event.orderId(), e);
            // Don't acknowledge - will retry
        }
    }

}

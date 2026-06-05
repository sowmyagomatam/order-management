package com.ecommerce.order.messaging;

import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.events.order.OrderConfirmedEvent;
import com.ecommerce.events.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.ecommerce.order.config.KafkaTopicConfig.ORDERS_CANCELLED_TOPIC;
import static com.ecommerce.order.config.KafkaTopicConfig.ORDERS_CONFIRMED_TOPIC;
import static com.ecommerce.order.config.KafkaTopicConfig.ORDERS_CREATED_TOPIC;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final KafkaTemplate<String, OrderCancelledEvent> orderCancelledEventKafkaTemplate;
    private final KafkaTemplate<String, OrderConfirmedEvent> orderConfirmedEventKafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for order: {}", event.orderId());

        kafkaTemplate.send(ORDERS_CREATED_TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published OrderCreatedEvent for order: {} to topic: {} partition: {}",
                                event.orderId(),
                                ORDERS_CREATED_TOPIC,
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to publish OrderCreatedEvent for order: {}",
                                event.orderId(), ex);
                    }
                });
    }

    public void publishOrderCancelledEvent(OrderCancelledEvent event){
        log.info("Publishing OrderCancelledEvent for order: {}", event.orderId());

        orderCancelledEventKafkaTemplate.send(ORDERS_CANCELLED_TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published OrderCancelledEvent for order: {} to topic: {} partition: {}",
                                event.orderId(),
                                ORDERS_CANCELLED_TOPIC,
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to publish OrderCancelledEvent for order: {}",
                                event.orderId(), ex);
                    }
                });
    }

    public void publishOrderConfirmedEvent(OrderConfirmedEvent event){
        log.info("Publishing OrderConfirmedEvent for order: {}", event.orderId());

        orderConfirmedEventKafkaTemplate.send(ORDERS_CONFIRMED_TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published OrderConfirmedEvent for order: {} to topic: {} partition: {}",
                                event.orderId(),
                                ORDERS_CONFIRMED_TOPIC,
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to publish OrderConfirmedEvent for order: {}",
                                event.orderId(), ex);
                    }
                });
    }

}

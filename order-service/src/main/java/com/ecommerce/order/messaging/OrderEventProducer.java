package com.ecommerce.order.messaging;

import com.ecommerce.events.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.ecommerce.order.config.KafkaTopicConfig.ORDERS_CREATED_TOPIC;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

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

}

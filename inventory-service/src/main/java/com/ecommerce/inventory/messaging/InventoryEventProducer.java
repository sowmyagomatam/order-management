package com.ecommerce.inventory.messaging;

import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryEventProducer {

    public static final String RESERVED_TOPIC = "inventory.reserved";
    private static final String FAILED_TOPIC = "inventory.reservation-failed";
    private final KafkaTemplate<String, InventoryReservedEvent> reservedEventKafkaTemplate;
    private final KafkaTemplate<String, InventoryReservationFailedEvent> failedEventKafkaTemplate;

    public void publishInventoryReservedEvent(InventoryReservedEvent event){
        log.info("Publishing InventoryReservedEvent for order: {}", event.orderId());
        reservedEventKafkaTemplate.send(RESERVED_TOPIC,event.orderId(),event)
                .whenComplete((result,ex) -> {
                            if (ex == null) {
                                log.info("Successfully published InventoryReservedEvent for order: {} to topic: {} partition: {}",
                                        event.orderId(),
                                        RESERVED_TOPIC,
                                        result.getRecordMetadata().partition());
                            } else {
                                log.error("Failed to publish InventoryReservedEvent for order: {}",
                                        event.orderId(), ex);
                            }
                        }
                        )
                .exceptionally( e -> {
                    log.error("Error publishing InventoryReservedEvent for order: {}", event.orderId(), e);
                    throw new RuntimeException("Failed to publish inventory reserved event", e);
                }
                );
    }

    public void publishInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.info("Publishing InventoryReservationFailedEvent for order: {}", event.orderId());
        failedEventKafkaTemplate.send(FAILED_TOPIC,event.orderId(),event)
                .whenComplete((result,ex) -> {
                            if (ex == null) {
                                log.info("Successfully published InventoryFailedReservedEvent for order: {} to topic: {} partition: {}",
                                        event.orderId(),
                                        FAILED_TOPIC,
                                        result.getRecordMetadata().partition());
                            } else {
                                log.error("Failed to publish InventoryFailedReservedEvent for order: {}",
                                        event.orderId(), ex);
                            }
                        }
                )
                .exceptionally( e -> {
                            log.error("Error publishing InventoryFailedReservedEvent for order: {}", event.orderId(), e);
                            throw new RuntimeException("Failed to publish inventory failed reserved event", e);
                        }
                );
    }
}

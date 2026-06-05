package com.ecommerce.inventory.messaging;

import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.events.order.OrderConfirmedEvent;
import com.ecommerce.events.order.OrderCreatedEvent;
import com.ecommerce.inventory.dto.ReservationResult;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaListener(
            topics = "orders.created",
            groupId = "inventory-service",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
           @Payload OrderCreatedEvent event,
           @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
           @Header(KafkaHeaders.OFFSET) long offset,
           Acknowledgment acknowledgment
    ){
        log.info("Received OrderCreatedEvent for order: {} from partition: {} offset: {}",
                event.orderId(), partition, offset);
        //reserve stock for this order (all-or-nothing)
        try {
            ReservationResult result = inventoryService.reserveOrder(event.orderId(), event.items());

            //Publish events
            publishEvents(event, result);
            log.info("Completed processing OrderCreatedEvent for order: {}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged message for order: {}", event.orderId());
            }
        } catch (Exception e) {
            log.error("Critical error processing OrderCreatedEvent for order: {}. Message will NOT be acknowledged.",
                    event.orderId(), e);
        }
    }

    @KafkaListener(
            topics = "orders.cancelled",
            groupId = "inventory-service",
            containerFactory = "orderCancelledEventKafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(
            @Payload OrderCancelledEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received OrderCancelledEvent for order: {} reason: {} from partition: {} offset: {}",
                event.orderId(), event.reason(), partition, offset);
        //release any stock reserved for this order
        try {
            inventoryService.releaseReservation(event.orderId());
            log.info("Completed processing OrderCancelledEvent for order: {}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged OrderCancelledEvent for order: {}", event.orderId());
            }
        } catch (Exception e) {
            log.error("Critical error releasing reservation for cancelled order: {}. Message will NOT be acknowledged.",
                    event.orderId(), e);
        }
    }

    @KafkaListener(
            topics = "orders.confirmed",
            groupId = "inventory-service",
            containerFactory = "orderConfirmedEventKafkaListenerContainerFactory"
    )
    public void handleOrderConfirmed(
            @Payload OrderConfirmedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received OrderConfirmedEvent for order: {} from partition: {} offset: {}",
                event.orderId(), partition, offset);
        //confirm the reservation for this fulfilled order
        try {
            inventoryService.confirmOrderReservations(event.orderId());
            log.info("Completed processing OrderConfirmedEvent for order: {}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged OrderConfirmedEvent for order: {}", event.orderId());
            }
        } catch (Exception e) {
            log.error("Critical error confirming reservation for fulfilled order: {}. Message will NOT be acknowledged.",
                    event.orderId(), e);
        }
    }

    private void publishEvents(OrderCreatedEvent event, ReservationResult result) {
        if(!result.failedItems().isEmpty()){
            inventoryEventProducer.publishInventoryReservationFailed(
                    new InventoryReservationFailedEvent(event.orderId(),
                            result.failedItems(),
                            String.format("%d of %d items failed reservation", result.failedItems().size(), event.items().size()),
                            Instant.now())
            );
        } else {
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    event.orderId(),
                    result.reservedItems(),
                    Instant.now()
            );
            inventoryEventProducer.publishInventoryReservedEvent(reservedEvent);
        }
    }
}

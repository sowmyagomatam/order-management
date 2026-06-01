package com.ecommerce.inventory.messaging;

import com.ecommerce.events.inventory.FailedItemEvent;
import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.inventory.ReservedItemEvent;
import com.ecommerce.events.order.OrderCreatedEvent;
import com.ecommerce.events.order.OrderItemEvent;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.ProductNotFoundException;
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
import java.util.ArrayList;
import java.util.List;

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
        //reserve stock for this order
        try {
            List<ReservedItemEvent> reservedItems = new ArrayList<>();
            List<FailedItemEvent> failedItems = new ArrayList<>();
            for (OrderItemEvent item : event.items()) {
                try {
                    inventoryService.reserveStock(item.productId(), item.quantity());

                    reservedItems.add(new ReservedItemEvent(
                            item.productId(),
                            item.productSku(),
                            item.unitPrice(),
                            item.quantity()
                    ));
                    log.info("Successfully reserved {} units of product: {} for order: {}",
                            item.quantity(), item.productId(), event.orderId());

                } catch(ProductNotFoundException e) {
                    log.error("Product not found: {} for order: {}. Item will be skipped.",
                            item.productId(), event.orderId(), e);
                    failedItems.add(new FailedItemEvent(
                            item.productId(),
                            item.productSku(),
                            item.quantity(),
                            0,
                            "Product not found"
                    ));

                }catch (InsufficientStockException e) {
                    log.error("Insufficient stock for product: {} (requested: {}) for order: {}. Item will be skipped.",
                            item.productId(), item.quantity(), event.orderId(), e);
                    failedItems.add(new FailedItemEvent(
                            item.productId(),
                            item.productSku(),
                            item.quantity(),
                            e.getAvailable(),
                            String.format("Insufficient stock. Available: %d, Requested: %d",
                                    e.getAvailable(), item.quantity())
                    ));

                } catch (Exception e) {
                    log.error("Unexpected error reserving stock for product: {} in order: {}",
                            item.productId(), event.orderId(), e);
                    failedItems.add(new FailedItemEvent(
                            item.productId(),
                            item.productSku(),
                            item.quantity(),
                            0,
                            "Unexpected error: " + e.getMessage()
                    ));
                }
            }
            //Publish events
            publishEvents(event, reservedItems, failedItems);
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

    private void publishEvents(OrderCreatedEvent event, List<ReservedItemEvent> reservedItems, List<FailedItemEvent> failedItems) {
        if(!failedItems.isEmpty()){
            inventoryEventProducer.publishInventoryReservationFailed(
                    new InventoryReservationFailedEvent(event.orderId(),
                            failedItems,
                            String.format("%d of %d items failed reservation", failedItems.size(), event.items().size()),
                            Instant.now())
            );
        } else {
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    event.orderId(),
                    reservedItems,
                    Instant.now()
            );
            inventoryEventProducer.publishInventoryReservedEvent(reservedEvent);
        }
    }
}

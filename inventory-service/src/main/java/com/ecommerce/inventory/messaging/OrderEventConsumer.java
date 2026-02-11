package com.ecommerce.inventory.messaging;

import com.ecommerce.common.dto.event.OrderCreatedEvent;
import com.ecommerce.common.dto.event.OrderItemEvent;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final InventoryService inventoryService;

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
            for (OrderItemEvent item : event.items()) {
                try {
                    inventoryService.reserveStock(item.productId(), item.quantity());
                    log.info("Successfully reserved {} units of product: {} for order: {}",
                            item.quantity(), item.productId(), event.orderId());
                } catch(ProductNotFoundException e) {
                    log.error("Product not found: {} for order: {}. Item will be skipped.",
                            item.productId(), event.orderId(), e);

                }catch (InsufficientStockException e) {
                    log.error("Insufficient stock for product: {} (requested: {}) for order: {}. Item will be skipped.",
                            item.productId(), item.quantity(), event.orderId(), e);

                } catch (Exception e) {
                    log.error("Unexpected error reserving stock for product: {} in order: {}",
                            item.productId(), event.orderId(), e);
                }
            }
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
}

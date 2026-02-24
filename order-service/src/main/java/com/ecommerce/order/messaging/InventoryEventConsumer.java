package com.ecommerce.order.messaging;

import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.order.domain.CancellationReason;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
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
public class InventoryEventConsumer {

    private final OrderRepository orderRepository;

    /**
     * Handle successful inventory reservation
     */
    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "order-service",
            containerFactory = "inventoryReservedEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReserved(
            @Payload InventoryReservedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received InventoryReservedEvent for order: {} from partition: {} offset: {}",
                event.orderId(), partition, offset);

        try {
            // Find the order
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

            log.info("Order {} current status: {}", event.orderId(), order.getStatus());

            // Update order status to INVENTORY_RESERVED
            order.updateStatus(OrderStatus.INVENTORY_RESERVED);
            orderRepository.save(order);

            log.info("Order {} status updated to INVENTORY_RESERVED. Reserved items: {}",
                    event.orderId(), event.reservedItems().size());

            // Acknowledge message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged InventoryReservedEvent for order: {}", event.orderId());
            }

        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent for order: {}",
                    event.orderId(), e);
            // Don't acknowledge - will retry
        }
    }

    /**
     * Handle failed inventory reservation
     */
    @KafkaListener(
            topics = "inventory.reservation-failed",
            groupId = "order-service",
            containerFactory = "inventoryReservedFailedEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReservationFailed(
            @Payload InventoryReservationFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received InventoryReservationFailedEvent for order: {} from partition: {} offset: {}",
                event.orderId(), partition, offset);

        try {
            // Find the order
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

            log.info("Order {} current status: {}", event.orderId(), order.getStatus());

            // Update order status to OUT_OF_STOCK
            order.cancel(CancellationReason.OUT_OF_STOCK, "System");
            orderRepository.save(order);

            log.warn("Order {} cancelled due to insufficient stock. Reason: {}. Failed items: {}",
                    event.orderId(), event.reason(), event.failedItems().size());

            // Log details of failed items
            event.failedItems().forEach(item -> {
                log.warn("  - Product {}: {} (Requested: {}, Available: {})",
                        item.productSku(),
                        item.reason(),
                        item.requestedQuantity(),
                        item.availableQuantity());
            });

            // Acknowledge message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Acknowledged InventoryReservationFailedEvent for order: {}", event.orderId());
            }

        } catch (Exception e) {
            log.error("Error processing InventoryReservationFailedEvent for order: {}",
                    event.orderId(), e);
            // Don't acknowledge - will retry
        }
    }
}
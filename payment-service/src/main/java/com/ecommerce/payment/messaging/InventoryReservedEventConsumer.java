package com.ecommerce.payment.messaging;

import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.order.OrderItemEvent;
import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
import com.ecommerce.events.payment.PaymentProcessingEvent;
import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.domain.command.CreatePaymentCommand;
import com.ecommerce.payment.domain.command.ProcessPaymentCommand;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryReservedEventConsumer {

    @Autowired
    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "payment-service",
            containerFactory = "inventoryReservedListenerContainerFactory"
    )
    public void handleInventoryReservedEvent(InventoryReservedEvent event,
                                             Acknowledgment ack){
        log.info("Received InventoryReservedEvent for order: {}", event.orderId());
        try {

            //create a payment for this event
            CreatePaymentCommand createPaymentCommand = CreatePaymentCommand.builder()
                    .orderId(event.orderId())
                    .amount(getAmount(event))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();
            Payment payment = paymentService.createPayment(createPaymentCommand);
            log.info("Payment created: {} for order: {}", payment.getId(), event.orderId());

            paymentEventProducer.publishPaymentProcessing(new PaymentProcessingEvent(
                    payment.getId(), event.orderId(), Instant.now()
            ));

            //process payment
            Payment processedPayment = paymentService.processPayment(ProcessPaymentCommand.builder()
                    .paymentId(payment.getId())
                    .build());
            log.info("Payment processed: {} with status: {}",
                    processedPayment.getId(), processedPayment.getPaymentStatus());

            //publish processed payment event
            if (processedPayment.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
                publishPaymentCompleted(processedPayment, event);
            } else {
                publishPaymentFailed(processedPayment, event);
            }
            if(ack != null){
                ack.acknowledge();
            }
        } catch (Exception e) {
        log.error("Error processing InventoryReservationFailedEvent in payments for order: {}",
                event.orderId(), e);
        // Don't acknowledge - will retry
    }



    }

    private void publishPaymentFailed(Payment processedPayment, InventoryReservedEvent event) {
        // Map reserved items to order items for compensation
        List<OrderItemEvent> orderItems = event.reservedItems().stream()
                .map(item -> OrderItemEvent.builder()
                        .productId(item.productId())
                        .quantity(item.quantityReserved())
                        .unitPrice(item.price())
                        .build())
                .toList();

        paymentEventProducer.publishPaymentFailed(new PaymentFailedEvent(
                processedPayment.getId(),
                processedPayment.getOrderId(),
                processedPayment.getAmount(),
                processedPayment.getFailureReason(),
                orderItems,
                Instant.now()
        ));
        log.info("Published PaymentFailedEvent for order: {}, reason: {}",
                processedPayment.getOrderId(), processedPayment.getFailureReason());
    }

    private void publishPaymentCompleted(Payment processedPayment, InventoryReservedEvent event) {
        paymentEventProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                processedPayment.getId(),
                processedPayment.getOrderId(),
                processedPayment.getAmount(),
                processedPayment.getPaymentMethod().toString(),
                processedPayment.getPaymentReference(),
                Instant.now()
        ));
        log.info("Published PaymentCompletedEvent for order: {}", processedPayment.getOrderId());

    }

    private BigDecimal getAmount(InventoryReservedEvent event) {
        return event.reservedItems().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantityReserved())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

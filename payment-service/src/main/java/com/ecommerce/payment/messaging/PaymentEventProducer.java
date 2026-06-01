package com.ecommerce.payment.messaging;

import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    private final KafkaTemplate<String, PaymentCompletedEvent> paymentCompletedEventKafkaTemplate;
    private final KafkaTemplate<String, PaymentFailedEvent> paymentFailedEventKafkaTemplate;

    /**
     * Publish payment completed event
     * Uses orderId as partition key for event ordering
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent for order: {}, payment: {}",
                event.orderId(), event.paymentId());

        paymentCompletedEventKafkaTemplate.send(PAYMENT_COMPLETED_TOPIC,
                event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PaymentCompletedEvent published successfully for order: {}, partition: {}, offset: {}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish PaymentCompletedEvent for order: {}",
                                event.orderId(), ex);
                    }
                })
                .exceptionally( e -> {
                            log.error("Error publishing PaymentCompletedEvent for order: {}", event.orderId(), e);
                            throw new RuntimeException("Failed to publish payment completed event", e);
                        }
                );
    }

    /**
     * Publish payment failed event
     * Uses orderId as partition key for event ordering
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for order: {}, payment: {}, reason: {}",
                event.orderId(), event.paymentId(), event.failureReason());

        paymentFailedEventKafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PaymentFailedEvent published successfully for order: {}, partition: {}, offset: {}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish PaymentFailedEvent for order: {}",
                                event.orderId(), ex);
                    }
                })
                .exceptionally( e -> {
                            log.error("Error publishing PaymentFailedEvent for order: {}", event.orderId(), e);
                            throw new RuntimeException("Failed to publish payment failed event", e);
                        }
                );;
    }

}

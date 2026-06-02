package com.ecommerce.order.config;

import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
import com.ecommerce.events.payment.PaymentProcessingEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class PaymentConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Connection
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Deserialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Performance tuning
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        return props;
    }

    @Bean
    public ConsumerFactory<String, PaymentProcessingEvent> paymentProcessingEventConsumerFactory() {
        JsonDeserializer<PaymentProcessingEvent> deserializer =
                new JsonDeserializer<>(PaymentProcessingEvent.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessingEvent>
    paymentProcessingListenerContainerFactory(
            ConsumerFactory<String, PaymentProcessingEvent> paymentProcessingEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentProcessingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentProcessingEventConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );
        return factory;
    }

    /**
     * Consumer Factory for PaymentCompletedEvent
     */
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedEventConsumerFactory() {
        JsonDeserializer<PaymentCompletedEvent> deserializer =
                new JsonDeserializer<>(PaymentCompletedEvent.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    /**
     * Kafka Listener Container Factory for PaymentCompletedEvent
     * Handles concurrent message processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
    paymentCompletedEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentCompletedEventConsumerFactory);

        // Concurrency - number of consumer threads
        factory.setConcurrency(3);

        // Manual acknowledgement mode for better control
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedEventConsumerFactory() {
        JsonDeserializer<PaymentFailedEvent> deserializer =
                new JsonDeserializer<>(PaymentFailedEvent.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentFailedEvent> paymentFailedEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentFailedEventConsumerFactory);

        // Concurrency - number of consumer threads
        factory.setConcurrency(3);

        // Manual acknowledgement mode for better control
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );

        return factory;
    }
}

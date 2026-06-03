package com.ecommerce.order.config;

import com.ecommerce.events.order.OrderCancelledEvent;
import com.ecommerce.events.order.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class OrderProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    /**
     * Producer factory for order created event
     * @return
     */
    @Bean
    public ProducerFactory<String, OrderCreatedEvent> orderEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> orderEventKafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent> orderEventProducerFactory) {
        return new KafkaTemplate<>(orderEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, OrderCancelledEvent> orderCancelledEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, OrderCancelledEvent> orderCancelledEventKafkaTemplate(
            ProducerFactory<String, OrderCancelledEvent> orderCancelledEventProducerFactory) {
        return new KafkaTemplate<>(orderCancelledEventProducerFactory);
    }

    private Map<String, Object> producerConfigs() {

        Map<String, Object> props = new HashMap<>();

        // Connection
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability - Wait for all replicas to acknowledge
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Retry configuration
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        // Idempotence - Prevents duplicate messages
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance tuning
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Timeout configuration
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return props;
    }
}

package com.ecommerce.payment.config;

import com.ecommerce.events.inventory.InventoryReservationFailedEvent;
import com.ecommerce.events.inventory.InventoryReservedEvent;
import com.ecommerce.events.payment.PaymentCompletedEvent;
import com.ecommerce.events.payment.PaymentFailedEvent;
import com.ecommerce.events.payment.PaymentProcessingEvent;
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
public class PaymentProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // serializaation settings
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        //reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        //performance settings
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        //timeout settings
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return props;
    }

    @Bean
    public ProducerFactory<String, PaymentProcessingEvent> paymentProcessingEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PaymentProcessingEvent> paymentProcessingEventKafkaTemplate(
            ProducerFactory<String, PaymentProcessingEvent> paymentProcessingEventProducerFactory) {
        return new KafkaTemplate<>(paymentProcessingEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, PaymentCompletedEvent> paymentCompletedEventProducerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PaymentCompletedEvent> paymentCompletedEventKafkaTemplate(
            ProducerFactory<String, PaymentCompletedEvent> paymentCompletedEventProducerFactory) {
        return new KafkaTemplate<>(paymentCompletedEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, PaymentFailedEvent> paymentFailedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PaymentFailedEvent> paymentFailedEventKafkaTemplate(
            ProducerFactory<String, PaymentFailedEvent> paymentFailedEventProducerFactory) {
        return new KafkaTemplate<>(paymentFailedEventProducerFactory);
    }
}

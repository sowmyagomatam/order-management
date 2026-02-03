package com.ecommerce.order.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_CREATED_TOPIC = "orders.created";
    public static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    public static final String INVENTORY_RESERVATION_FAILED_TOPIC = "inventory.reservation-failed";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic: orders.created
     * Published when a new order is created
     */
    @Bean
    public NewTopic ordersCreatedTopic() {
        return TopicBuilder.name(ORDERS_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic: inventory.reserved
     * Published by inventory-service when stock is reserved
     *
     */
    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(INVENTORY_RESERVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic: inventory.reservation-failed
     * Published when stock reservation fails
     */
    @Bean
    public NewTopic inventoryReservationFailedTopic() {
        return TopicBuilder.name(INVENTORY_RESERVATION_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

}

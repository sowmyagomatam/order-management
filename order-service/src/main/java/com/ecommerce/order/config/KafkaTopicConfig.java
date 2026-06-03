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
    public static final String ORDERS_CANCELLED_TOPIC = "orders.cancelled";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

   /* @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    */

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
     * Topic: orders.cancelled
     * Published when an order is cancelled
     * @return
     */
    @Bean
    public NewTopic ordersCancelledTopic() {
        return TopicBuilder.name(ORDERS_CANCELLED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }


}

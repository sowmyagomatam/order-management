package com.ecommerce.payment.config;

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
public class PaymentTopicConfig {
    public static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

  /*  @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

   */

    /**
     * Topic: payment.completed
     * Published when payment is successfully processed
     * Consumed by: order-service
     */
    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(PAYMENT_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic: payment.failed
     * Published when payment processing fails
     * Consumed by: order-service, inventory-service
     */
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

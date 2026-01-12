package com.ecommerce.order.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class TestConfig {


    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    static {
        postgresContainer.start();
    }

}

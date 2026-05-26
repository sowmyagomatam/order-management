package com.ecommerce.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class PaymentExecutorConfig {

    @Value("${payment.executor.pool-size:10}")
    private int poolSize;

    @Bean(name = "paymentExecutor", destroyMethod = "shutdown")
    public ExecutorService paymentExecutor() {
        log.info("Creating payment gateway executor with {} threads", poolSize);

        return Executors.newFixedThreadPool(
                poolSize,
                new PaymentGatewayThreadFactory()
        );
    }
    private static class PaymentGatewayThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("payment-gateway-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("Uncaught exception in payment gateway thread: {}", t.getName(), e)
            );
            return thread;
        }
    }
}

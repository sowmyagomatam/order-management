package com.ecommerce.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class PaymentExecutorConfig {

    @Value("${payment.executor.pool-size:10}")
    private int poolSize;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService getPaymentExecutor(){

        return Executors.newFixedThreadPool(
                poolSize,
                new PaymentThreadFactory()
        );
    }

    private static class PaymentThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("payment-gateway-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}

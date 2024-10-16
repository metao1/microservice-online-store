package com.metao.book.order.application.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Runner {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static <K, V> void send(
        KafkaTemplate<K, V> runnable, String topic, K key, V event
    ) {
        executor.submit(() -> runnable.send(topic, key, event));
    }

    @EventListener
    public void run(ContextClosedEvent event) {
        // Perform cleanup logic here, e.g., closing resources or waiting for tasks to complete
        log.info("Application is shutting down gracefully...");
        // Simulate waiting for in-flight tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.error("Failed to wait for tasks to complete. forcing shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Failed to wait for tasks to complete: {}", e.getMessage());
        }
        log.info("Resources cleaned up and application shut down.");
    }
}

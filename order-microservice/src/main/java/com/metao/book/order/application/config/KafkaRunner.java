package com.metao.book.order.application.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Runner helps to submit Kafka messages asynchronously using lightweight Executor On event of
 * {@link ContextClosedEvent} the application will gracefully shut down and wait for in-flight tasks to complete.
 */
@Slf4j
@Component
public class KafkaRunner {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static <K, V> void submit(
        KafkaTemplate<K, V> kafkaTemplate, String topic, K key, V event
    ) {
        executor.submit(() -> kafkaTemplate.send(topic, key, event));
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

package com.metao.book.product.infrastructure.factory.handler;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.metao.book.product.application.port.ProcessedInventoryEventPort;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventCommand;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventUseCase;
import com.metao.book.shared.ProductUpdatedEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("ProductKafkaListenerComponent idempotency")
class ProductKafkaListenerIdempotencyIT {

    private HandleProductUpdatedEventCommand inventoryReductionCommand(String key, double volume) {
        return new HandleProductUpdatedEventCommand(
            key,
            "SKU-1",
            "INVENTORY_REDUCTION",
            BigDecimal.valueOf(volume)
        );
    }

    @Test
    @DisplayName("duplicate messages with same key are processed once")
    void duplicateMessagesAreSkipped() {
        var productService = mock(ProductDomainService.class);
        var processedRepo = mock(ProcessedInventoryEventPort.class);
        var useCase = new HandleProductUpdatedEventUseCase(productService, processedRepo);

        var firstCall = new AtomicBoolean(true);
        Mockito.when(processedRepo.markProcessed("order-1:SKU-1"))
            .thenAnswer(inv -> firstCall.getAndSet(false));

        var command = inventoryReductionCommand("order-1:SKU-1", 2.0);

        useCase.handle(command);
        useCase.handle(command);

        verify(productService, times(1)).reduceProductVolumeAtomically(eq("SKU-1"), eq(BigDecimal.valueOf(2.0)));
    }

    @Test
    @DisplayName("simultaneous duplicate messages still processed once")
    void concurrentDuplicatesStillIdempotent() throws InterruptedException {
        var productService = mock(com.metao.book.product.application.service.ProductDomainService.class);
        var processedRepo = mock(ProcessedInventoryEventPort.class);
        var useCase = new HandleProductUpdatedEventUseCase(productService, processedRepo);

        var firstCall = new AtomicBoolean(true);
        org.mockito.Mockito.when(processedRepo.markProcessed("order-2:SKU-1"))
            .thenAnswer(inv -> firstCall.getAndSet(false));

        var command = inventoryReductionCommand("order-2:SKU-1", 3.0);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var latch = new CountDownLatch(2);
            pool.submit(() -> {
                useCase.handle(command);
                latch.countDown();
            });
            pool.submit(() -> {
                useCase.handle(command);
                latch.countDown();
            });
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verify(productService, times(1)).reduceProductVolumeAtomically(eq("SKU-1"),
                    eq(BigDecimal.valueOf(3.0)))
            );
        }
    }
}

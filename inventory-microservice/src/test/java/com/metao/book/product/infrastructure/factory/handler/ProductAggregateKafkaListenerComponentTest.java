package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.infrastructure.persistence.repository.ProcessedInventoryEventRepository;
import com.metao.book.shared.ProductUpdatedEvent;
import java.math.BigDecimal;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductKafkaListenerComponent")
class ProductAggregateKafkaListenerComponentTest {

    @Mock
    private ProductDomainService productService;

    @Mock
    private ProcessedInventoryEventRepository processedInventoryEventRepository;

    @InjectMocks
    private ProductKafkaListenerComponent listener;

    @Nested
    @DisplayName("onProductUpdateEvent")
    class OnProductAggregateUpdateEvent {

        @Test
        @DisplayName("should reduce inventory for inventory-reduction marker event")
        void shouldReduceInventoryForInventoryReductionMarkerEvent() {
            ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                .setSku("SKU-1")
                .setDescription("INVENTORY_REDUCTION")
                .setVolume(3.0)
                .build();
            ConsumerRecord<String, ProductUpdatedEvent> record = new ConsumerRecord<>(
                "product-updated", 0, 0L, "order-1:SKU-1", event);

            when(processedInventoryEventRepository.markProcessed("order-1:SKU-1")).thenReturn(true);

            listener.onProductUpdateEvent(record);

            verify(productService).reduceProductVolumeAtomically(eq("SKU-1"), eq(BigDecimal.valueOf(3.0)));
        }

        @Test
        @DisplayName("should skip duplicate inventory-reduction event")
        void shouldSkipDuplicateInventoryReductionEvent() {
            ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                .setSku("SKU-1")
                .setDescription("INVENTORY_REDUCTION")
                .setVolume(3.0)
                .build();
            ConsumerRecord<String, ProductUpdatedEvent> record = new ConsumerRecord<>(
                "product-updated", 0, 0L, "order-1:SKU-1", event);

            when(processedInventoryEventRepository.markProcessed("order-1:SKU-1")).thenReturn(false);

            listener.onProductUpdateEvent(record);

            verify(productService, never()).reduceProductVolumeAtomically(any(), any());
        }

        @Test
        @DisplayName("should ignore non-inventory product-updated events")
        void shouldIgnoreNonInventoryProductUpdatedEvents() {
            ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                .setSku("SKU-1")
                .setDescription("NORMAL_PRODUCT_UPDATE")
                .setVolume(3.0)
                .build();
            ConsumerRecord<String, ProductUpdatedEvent> record = new ConsumerRecord<>(
                "product-updated", 0, 0L, "any-key", event);

            listener.onProductUpdateEvent(record);

            verify(processedInventoryEventRepository, never()).markProcessed(any());
            verify(productService, never()).reduceProductVolumeAtomically(any(), any());
        }
    }
}

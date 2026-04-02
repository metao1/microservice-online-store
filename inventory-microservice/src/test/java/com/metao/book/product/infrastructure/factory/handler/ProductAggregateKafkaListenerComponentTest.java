package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.Mockito.verify;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.usecase.HandleProductCreatedEventCommand;
import com.metao.book.product.application.usecase.HandleProductCreatedEventUseCase;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventCommand;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventUseCase;
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
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductKafkaListenerComponent")
class ProductAggregateKafkaListenerComponentTest {

    @Mock
    private HandleProductCreatedEventUseCase handleProductCreatedEventUseCase;

    @Mock
    private HandleProductUpdatedEventUseCase handleProductUpdatedEventUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ProductKafkaListenerComponent listener;

    @Nested
    @DisplayName("onProductCreatedEvent")
    class OnProductCreatedEvent {

        @Test
        @DisplayName("should map created record to use case command")
        void shouldMapCreatedRecordToUseCaseCommand() {
            ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
                .setSku("SKU-1")
                .build();
            ConsumerRecord<String, ProductCreatedEvent> record = new ConsumerRecord<>(
                "product-created", 0, 0L, "event-1", event);

            listener.onProductCreatedEvent(record, acknowledgment);

            verify(handleProductCreatedEventUseCase).handle(new HandleProductCreatedEventCommand("event-1", "SKU-1"));
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("onProductUpdateEvent")
    class OnProductAggregateUpdateEvent {

        @Test
        @DisplayName("should map updated record to use case command")
        void shouldMapUpdatedRecordToUseCaseCommand() {
            ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                .setSku("SKU-1")
                .setDescription("INVENTORY_REDUCTION")
                .setVolume(3.0)
                .build();
            ConsumerRecord<String, ProductUpdatedEvent> record = new ConsumerRecord<>(
                "product-updated", 0, 0L, "order-1:SKU-1", event);

            listener.onProductUpdateEvent(record, acknowledgment);

            verify(handleProductUpdatedEventUseCase).handle(new HandleProductUpdatedEventCommand(
                "order-1:SKU-1",
                "SKU-1",
                "INVENTORY_REDUCTION",
                BigDecimal.valueOf(3.0)
            ));
            verify(acknowledgment).acknowledge();
        }
    }
}

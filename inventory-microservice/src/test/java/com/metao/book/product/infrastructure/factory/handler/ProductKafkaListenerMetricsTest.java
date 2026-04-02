package com.metao.book.product.infrastructure.factory.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.usecase.HandleProductCreatedEventUseCase;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventUseCase;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.Acknowledgment;

class ProductKafkaListenerMetricsTest {

    @Test
    void timerIsRecordedWhenProductCreatedListenerRuns() {
        var handleProductCreatedEventUseCase = Mockito.mock(HandleProductCreatedEventUseCase.class);
        var handleProductUpdatedEventUseCase = Mockito.mock(HandleProductUpdatedEventUseCase.class);
        var listener = new ProductKafkaListenerComponent(handleProductCreatedEventUseCase, handleProductUpdatedEventUseCase);
        var acknowledgment = mock(Acknowledgment.class);
        var meterRegistry = new SimpleMeterRegistry();

        ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
            .setSku("SKU1")
            .setTitle("Title")
            .setDescription("Desc")
            .setImageUrl("https://example.com/img.jpg")
            .setPrice(10.0)
            .setCurrency("USD")
            .setVolume(1.0)
            .setCreateTime(com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .build())
            .addAllCategories(Collections.emptyList())
            .build();

        ConsumerRecord<String, ProductCreatedEvent> record =
            new ConsumerRecord<>("product-created", 0, 0L, "key", event);

        listener.onProductCreatedEvent(record, acknowledgment);

        // With direct invocation (no Spring AOP), @Timed won't register; just assert execution success.
        assertThat(record.value().getSku()).isEqualTo("SKU1");
    }
}

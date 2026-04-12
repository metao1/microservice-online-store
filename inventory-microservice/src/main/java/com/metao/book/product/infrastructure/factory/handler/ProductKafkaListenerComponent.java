package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.usecase.HandleProductCreatedEventCommand;
import com.metao.book.product.application.usecase.HandleProductCreatedEventUseCase;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventCommand;
import com.metao.book.product.application.usecase.HandleProductUpdatedEventUseCase;
import com.metao.book.shared.ProductUpdatedEvent;
import io.micrometer.core.annotation.Timed;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ProductKafkaListenerComponent {

    private final HandleProductCreatedEventUseCase handleProductCreatedEventUseCase;
    private final HandleProductUpdatedEventUseCase handleProductUpdatedEventUseCase;

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-created.id}",
        topics = "${kafka.topic.product-created.name}",
        groupId = "${kafka.topic.product-created.group-id}",
        containerFactory = "productCreatedEventKafkaListenerContainerFactory")
    @Timed(value = "inventory.listener.product-created", extraTags = {"listener", "product-created"})
    public void onProductCreatedEvent(ConsumerRecord<String, ProductCreatedEvent> event, Acknowledgment acknowledgment) {
        handleProductCreatedEventUseCase.handle(new HandleProductCreatedEventCommand(
            event.key(),
            event.value().getSku()
        ));
        acknowledgment.acknowledge();
    }

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-updated.id}",
        topics = "${kafka.topic.product-updated.name}",
        groupId = "${kafka.topic.product-updated.group-id}",
        containerFactory = "productUpdatedEventKafkaListenerContainerFactory")
    @Timed(value = "inventory.listener.product-updated", extraTags = {"listener", "product-updated"})
    public void onProductUpdateEvent(ConsumerRecord<String, ProductUpdatedEvent> event, Acknowledgment acknowledgment) {
        try {
            handleProductUpdatedEventUseCase.handle(new HandleProductUpdatedEventCommand(
                event.key(),
                event.value().getSku(),
                event.value().getDescription(),
                BigDecimal.valueOf(event.value().getVolume())
            ));
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error(
                "Failed processing product-updated event: topic={}, key={}, sku={}, volume={}, description={}",
                event.topic(),
                event.key(),
                event.value().getSku(),
                event.value().getVolume(),
                event.value().getDescription(),
                ex
            );
            throw ex;
        }
    }

    @DltHandler
    public void onProductEventDlt(
        ConsumerRecord<String, ?> event,
        Acknowledgment acknowledgment,
        @Header(name = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionClass,
        @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage
    ) {
        log.error(
            "Product event sent to DLT: topic={}, key={}, value={}, causeClass={}, causeMessage={}",
            event.topic(),
            event.key(),
            event.value(),
            exceptionClass,
            exceptionMessage
        );
        acknowledgment.acknowledge();
    }
}

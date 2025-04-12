package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ProductKafkaListenerComponent {

    private final ProductDatabaseHandler productDatabaseHandler;

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-created.id}",
        topics = "${kafka.topic.product-created.name}",
        groupId = "${kafka.topic.product-created.group-id}",
        containerFactory = "productCreatedEventKafkaListenerContainerFactory")
    public void onProductCreatedEvent(ConsumerRecord<String, ProductCreatedEvent> event) {
        var productCreatedEvent = event.value();
        log.debug("Consumed {}", productCreatedEvent);
        productDatabaseHandler.accept(productCreatedEvent);
    }

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-updated.id}",
        topics = "${kafka.topic.product-updated.name}",
        groupId = "${kafka.topic.product-updated.group-id}",
        containerFactory = "productUpdatedEventKafkaListenerContainerFactory")
    public void onProductUpdateEvent(ConsumerRecord<String, ProductUpdatedEvent> event) {
        var productUpdatedEvent = event.value();
        log.debug("Consumed {}", productUpdatedEvent);
        productDatabaseHandler.accept(productUpdatedEvent);
    }
}

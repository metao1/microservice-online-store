package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.service.ProductApplicationService;
import com.metao.book.shared.CategoryOuterClass.Category;
import com.metao.book.shared.ProductUpdatedEvent;
import java.math.BigDecimal;
import java.util.Currency;
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

    private final ProductApplicationService productService;

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-created.id}",
        topics = "${kafka.topic.product-created.name}",
        groupId = "${kafka.topic.product-created.group-id}",
        containerFactory = "productCreatedEventKafkaListenerContainerFactory")
    public void onProductCreatedEvent(ConsumerRecord<String, ProductCreatedEvent> event) {
        var productCreatedEvent = event.value();
        log.debug("Consumed {}", productCreatedEvent);
        // This would require mapping from ProductCreatedEvent to CreateProductCommand
        var command = new CreateProductCommand(
            productCreatedEvent.getSku(),
            productCreatedEvent.getTitle(),
            productCreatedEvent.getDescription(),
            productCreatedEvent.getImageUrl(),
            BigDecimal.valueOf(productCreatedEvent.getPrice()),
            Currency.getInstance(productCreatedEvent.getCurrency()),
            BigDecimal.valueOf(productCreatedEvent.getVolume()),
            productCreatedEvent.getCategoriesList()
                .stream()
                .map(Category::getName)
                .toList()
        );
        productService.createProduct(command);
        log.info("Product created event received for SKU: {}", productCreatedEvent.getSku());
    }

    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-updated.id}",
        topics = "${kafka.topic.product-updated.name}",
        groupId = "${kafka.topic.product-updated.group-id}",
        containerFactory = "productUpdatedEventKafkaListenerContainerFactory")
    public void onProductUpdateEvent(ConsumerRecord<String, ProductUpdatedEvent> event) {
        var productUpdatedEvent = event.value();
        log.debug("Consumed {}", productUpdatedEvent);
        // TODO: Implement product update from Kafka event using ProductApplicationService
        // This would require mapping from ProductUpdatedEvent to UpdateProductCommand
        log.info("Product updated event received for SKU: {}", productUpdatedEvent.getSku());
    }
}

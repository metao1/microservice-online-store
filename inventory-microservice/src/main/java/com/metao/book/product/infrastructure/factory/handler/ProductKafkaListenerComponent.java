package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.infrastructure.persistence.repository.ProcessedInventoryEventRepository;
import com.metao.book.shared.CategoryOuterClass.Category;
import com.metao.book.shared.ProductUpdatedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ProductKafkaListenerComponent {

    private static final String INVENTORY_REDUCTION_MARKER = "INVENTORY_REDUCTION";

    private final ProductDomainService productService;
    private final ProcessedInventoryEventRepository processedInventoryEventRepository;

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
            Instant.ofEpochSecond(productCreatedEvent.getCreateTime().getSeconds(),
                productCreatedEvent.getCreateTime().getNanos()),

            productCreatedEvent.getCategoriesList()
                .stream()
                .map(Category::getName)
                .collect(Collectors.toSet())
        );
        productService.createProduct(command);
        log.info("Product created event received for SKU: {}", productCreatedEvent.getSku());
    }

    @Transactional
    @RetryableTopic(attempts = "1")
    @KafkaListener(id = "${kafka.topic.product-updated.id}",
        topics = "${kafka.topic.product-updated.name}",
        groupId = "${kafka.topic.product-updated.group-id}",
        containerFactory = "productUpdatedEventKafkaListenerContainerFactory")
    public void onProductUpdateEvent(ConsumerRecord<String, ProductUpdatedEvent> event) {
        var productUpdatedEvent = event.value();
        log.debug("Consumed {}", productUpdatedEvent);
        String eventId = event.key();
        if (eventId == null || eventId.isBlank()) {
            log.warn("Skipping ProductUpdatedEvent without idempotency key for sku {}", productUpdatedEvent.getSku());
            return;
        }

        if (!INVENTORY_REDUCTION_MARKER.equals(productUpdatedEvent.getDescription())) {
            log.info("Product updated event received for SKU: {}", productUpdatedEvent.getSku());
            return;
        }

        boolean firstProcessing = processedInventoryEventRepository.markProcessed(eventId);
        if (!firstProcessing) {
            log.info("Inventory reduction event {} already processed, skipping.", eventId);
            return;
        }

        productService.reduceProductVolumeAtomically(productUpdatedEvent.getSku(),
            BigDecimal.valueOf(productUpdatedEvent.getVolume()));

        log.info("Inventory reduced for sku {} by {} (event {}).",
            productUpdatedEvent.getSku(), productUpdatedEvent.getVolume(), eventId);
    }
}

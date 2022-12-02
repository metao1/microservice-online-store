package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductServiceInterface;
import com.metao.book.shared.Currency;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteProductService {

    @Value("${kafka.topic.product}")
    private String productEventTopic;
    private final ProductServiceInterface productService;
    private final KafkaTemplate<String, ProductsResponseEvent> kafkaTemplate;

    @Transactional
    public void handle(ProductsResponseEvent getProductEvent) {
        getProductEvent.getProducts().stream()
            .map(ProductEvent::getProductId)
            .map(ProductId::new)
            .map(productService::getProductById)
            .flatMap(Optional::stream)
            .map(ProductEntity::toDto)
            .forEach(product -> {
                var productEvent = mapToProductEvent(product);
                var productsResponseEvent = new ProductsResponseEvent(List.of(productEvent),
                    Instant.now().toEpochMilli());
                kafkaTemplate.send(productEventTopic, product.getAsin(), productsResponseEvent)
                    .acceptEitherAsync(new CompletableFuture<>(),
                        e -> log.error("Failed to send message:{}", e.getRecordMetadata())
                    );
            });
    }

    private ProductEvent mapToProductEvent(ProductDTO productDTO) {
        return ProductEvent.newBuilder()
                .setProductId(productDTO.getAsin())
                .setTitle(productDTO.getTitle())
                .setDescription(productDTO.getDescription())
                .setCurrency(Currency.dlr)
                .setPrice(productDTO.getPrice().doubleValue())
                .setImageUrl(productDTO.getImageUrl())
                .build();
    }
}

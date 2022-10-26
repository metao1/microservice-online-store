package com.metao.book.retails.infrastructure.factory.handler;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.shared.Currency;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

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
            .map(productService::getProductById)
            .flatMap(Optional::stream)
            .map(ProductEntity::toDto)
            .forEach(product -> {
                var productEvent = mapToProductEvent(product);
                var productsResponseEvent = new ProductsResponseEvent(List.of(productEvent),
                    Instant.now().toEpochMilli());
                kafkaTemplate.send(productEventTopic, product.getAsin(), productsResponseEvent)
                    .addCallback(new ListenableFutureCallback<>() {
                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("Failed to send message", ex);
                        }

                        @Override
                        public void onSuccess(SendResult<String, ProductsResponseEvent> result) {
                            log.info("Sent message with offset: {}", result.getRecordMetadata().offset());
                        }
                    });
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

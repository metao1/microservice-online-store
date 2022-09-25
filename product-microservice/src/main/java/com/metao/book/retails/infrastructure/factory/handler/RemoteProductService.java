package com.metao.book.retails.infrastructure.factory.handler;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.shared.GetProductEvent;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteProductService {
    private final ProductServiceInterface productService;
    private final KafkaTemplate<String, GetProductEvent> kafkaTemplate;

    @Transactional
    public void handle(GetProductEvent getProductEvent) {
        productService
                .getProductById(getProductEvent.getProductId())
                .map(ProductEntity::toDto)
                .ifPresent(product -> {
                    var productEvent = mapToProductEvent(product);
                    var productsResponseEvent = new ProductsResponseEvent(List.of(productEvent), Instant.now().toEpochMilli());
                    kafkaTemplate.send("products-response", product.getAsin(), productsResponseEvent)
                            .addCallback(
                                    new ListenableFutureCallback<>() {
                                        @Override
                                        public void onFailure(Throwable ex) {
                                            log.error("Failed to send message", ex);
                                        }

                                        @Override
                                        public void onSuccess(SendResult<String, GetProductEvent> result) {
                                            log.info("Sent message with offset: {}", result.getRecordMetadata().offset());
                                        }
                                    }
                            );
                });
    }

    private ProductEvent mapToProductEvent(ProductDTO productDTO) {

    }
}

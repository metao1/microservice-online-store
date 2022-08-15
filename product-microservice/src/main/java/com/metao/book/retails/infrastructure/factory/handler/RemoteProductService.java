package com.metao.book.retails.infrastructure.factory.handler;

import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.shared.GetProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Instant;

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
                    var productRequestedEvent = new GetProductEvent(product.getAsin(), Instant.now().toEpochMilli());
                    kafkaTemplate.send("product-request", productRequestedEvent.getProductId(), productRequestedEvent)
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
}

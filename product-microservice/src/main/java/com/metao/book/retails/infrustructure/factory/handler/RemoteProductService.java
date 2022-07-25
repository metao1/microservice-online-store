package com.metao.book.retails.infrustructure.factory.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.retails.domain.ProductServiceInterface;
import com.metao.book.retails.domain.event.GetProductEvent;
import com.metao.book.retails.domain.event.ProductRequestedEvent;
import com.metao.book.shared.domain.RemoteEvent;
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

    private final ObjectMapper objectMapper;
    private final ProductServiceInterface productService;
    private final KafkaTemplate<String, RemoteEvent> kafkaTemplate;

    @Transactional
    public void handle(GetProductEvent getProductEvent) {
        productService
                .getProductById(getProductEvent.productId())
                .map(ProductEntity::toDto)
                .ifPresent(product -> {
                    var productRequestedEvent = ProductRequestedEvent.builder()
                            .productId(new ProductId(product.getAsin()))
                            .occurredOn(Instant.now())
                            .build();
                    var remoteEvent = RemoteEvent.createRemoteEvent(productRequestedEvent, objectMapper);
                    kafkaTemplate.send("products", remoteEvent.getId(), remoteEvent)
                            .addCallback(
                                    new ListenableFutureCallback<>() {
                                        @Override
                                        public void onFailure(Throwable ex) {
                                            log.error("Failed to send message", ex);
                                        }

                                        @Override
                                        public void onSuccess(SendResult<String, RemoteEvent> result) {
                                            log.info("Sent message with offset: {}", result.getRecordMetadata().offset());
                                        }
                                    }
                            );
                });
    }
}

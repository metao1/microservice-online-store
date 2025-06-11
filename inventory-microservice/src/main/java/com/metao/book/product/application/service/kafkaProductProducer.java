package com.metao.book.product.application.service;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class kafkaProductProducer {

    @Value("${kafka.topic.product-created.name}")
    String productTopic;

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public Boolean sendEvent(ProductCreatedEvent event) {
        return StageProcessor.accept(event)
            .applyExceptionally((e, exp) -> {
                if (exp != null && event == null) {
                    log.warn("invalid event when saving product {}", exp.toString());
                    return false;
                }
                try {
                    kafkaTemplate.send(productTopic, event.getAsin(), event).get(10, TimeUnit.SECONDS);
                    return true;
                } catch (InterruptedException | TimeoutException | ExecutionException ex) {
                    Thread.currentThread().interrupt();
                    log.error("error when saving product {}", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            });
    }
}

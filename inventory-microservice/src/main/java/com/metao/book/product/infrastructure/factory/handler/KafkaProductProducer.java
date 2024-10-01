package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.event.ProductCreatedEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.isEnabled", havingValue = "true", matchIfMissing = true)
public class KafkaProductProducer {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final NewTopic productTopic;
    private int counter;

    public CompletableFuture<SendResult<String, ProductCreatedEvent>> publish(ProductCreatedEvent event) {
        log.info("Publishing product event: {}", counter++);
        return kafkaTemplate.send(productTopic.name(), event.getAsin(), event);
    }
}

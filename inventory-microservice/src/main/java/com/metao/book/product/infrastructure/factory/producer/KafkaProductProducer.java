package com.metao.book.product.infrastructure.factory.producer;

import com.metao.book.product.domain.event.ProductCreatedEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class KafkaProductProducer {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final NewTopic productTopic;

    public void sendToKafka(@Valid ProductCreatedEvent event) {
        kafkaTemplate.send(productTopic.name(), event.id(), event)
            .thenRun(() -> log.debug("product {} sent to Kafka", event));
    }
}

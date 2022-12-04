package com.metao.book.product.application.service;

import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
public class KafkaOrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void send(String topic, String orderId, OrderEvent order) {
        log.info("sending order='{}' to topic='{}'", order, topic);
        kafkaTemplate.send(topic, orderId, order)
            .addCallback(result -> log.info("Sent: {}",
                result != null ? result.getProducerRecord().value() : null), ex -> {
            });
    }
}

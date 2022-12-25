package com.metao.book.order.infrastructure.kafka;

import javax.validation.constraints.NotBlank;

import com.metao.book.shared.OrderEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final NewTopic orderTopic;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void produceOrderMessage(@NotBlank OrderEvent order) {
        log.info("sending order='{}' to topic='{}'", order, orderTopic.name());
        kafkaTemplate.send(orderTopic.name(), order.getOrderId(), order)
                .addCallback(
                        result -> log.info("Sent: {}", result != null ? result.getProducerRecord().value() : null),
                        ex -> log.error(ex.getMessage(), ex)
                        );
    }
}

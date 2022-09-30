package com.metao.book.order.infrastructure.kafka;


import com.metao.book.shared.OrderAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final KafkaTemplate<Long, OrderAvro> kafkaTemplate;

    public void send(String topic, @NotBlank Long orderId, @NotBlank OrderAvro order) {
        log.info("sending order='{}' to topic='{}'", order, topic);
        kafkaTemplate.send(topic, orderId, order)
                .addCallback(result -> log.info("Sent: {}",
                                result != null ? result.getProducerRecord().value() : null),
                        ex -> {
                        });
    }
}

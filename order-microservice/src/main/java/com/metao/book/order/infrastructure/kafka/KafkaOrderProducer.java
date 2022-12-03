package com.metao.book.order.infrastructure.kafka;

import com.metao.book.shared.OrderAvro;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final KafkaTemplate<String, OrderAvro> kafkaTemplate;

    public void send(String topic, @NotBlank String orderId, @NotBlank OrderAvro order) {
        log.info("sending order='{}' to topic='{}'", order, topic);
        kafkaTemplate.send(topic, orderId, order)
            .addCallback(result -> log.info("Sent: {}",
                result != null ? result.getProducerRecord().value() : null), ex -> {
            });
    }
}

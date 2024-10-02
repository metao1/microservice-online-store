package com.metao.book.order.infrastructure.kafka;

import com.google.protobuf.GeneratedMessageV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOrderProducer<T extends GeneratedMessageV3> {

    private final NewTopic orderTopic;
    private final KafkaTemplate<String, T> kafkaTemplate;

    public void sendToKafka(String key, T value) {
        kafkaTemplate.send(orderTopic.name(), key, value)
            .thenRun(() -> log.debug("Order {} sent", value));
    }
}

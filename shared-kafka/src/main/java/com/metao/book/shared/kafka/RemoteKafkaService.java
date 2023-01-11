package com.metao.book.shared.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RemoteKafkaService<S, T extends SpecificRecord> {

    private final KafkaTemplate<S, T> kafkaTemplate;

    public void sendToTopic(String topic, S key, T event) {
        kafkaTemplate.send(topic, key, event)
            .thenAccept(result -> log.info("Sent: {}",
                result != null ? result.getProducerRecord().value() : null));
    }
}

package com.metao.kafka;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventHandler {

    private final Map<String, KafkaTemplate<?, Object>> kafkaFactoryMap;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V> String send(String key, V e) {
        KafkaTemplate kafkaTemplate = kafkaFactoryMap.get(e.getClass().getName());

        if (kafkaTemplate == null) {
            throw new IllegalArgumentException("No KafkaTemplate configured for type: " + e.getClass().getName());
        }
        kafkaTemplate.sendDefault(key, e);
        return key;
    }
}
package com.metao.kafka;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventHandler{

    private final KafkaEventConfiguration kafkaEventConfiguration;

    public <V> String getKafkaTopic(Class<V> clazz) {
        var classToTopicMap = kafkaEventConfiguration.getClassToTopicMap();
        String eventClassName = clazz.getName();
        return Optional.ofNullable(classToTopicMap.get(eventClassName))
            .orElseThrow(() -> new IllegalArgumentException("No topic configured for event type: " + eventClassName));
    }
}
package com.metao.kafka;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@EnableKafka
@Component
public class KafkaEventHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, String> classToTopicMap;

    public KafkaEventHandler(KafkaTemplate<String, Object> kafkaTemplate, KafkaEventConfiguration eventConfiguration) {
        this.kafkaTemplate = kafkaTemplate;
        // Create a lookup map for efficiency: Class Name -> Topic Name
        this.classToTopicMap = new ConcurrentHashMap<>();
        eventConfiguration.getTopic().values().forEach(topicConfig ->
            classToTopicMap.put(topicConfig.classPath(), topicConfig.name())
        );
    }

    public void send(String key, Object event) {
        String eventClassName = event.getClass().getName();
        String topicName = Optional.ofNullable(classToTopicMap.get(eventClassName))
            .orElseThrow(() -> new IllegalArgumentException("No topic configured for event type: " + eventClassName));
        kafkaTemplate.setDefaultTopic(topicName);
        kafkaTemplate.sendDefault(key, event);
    }
}
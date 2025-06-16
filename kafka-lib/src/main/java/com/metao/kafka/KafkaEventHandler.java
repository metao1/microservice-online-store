package com.metao.kafka;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventHandler {

    @Lazy
    private final Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap;


    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) {
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            kafkaFactoryMap.forEach((c, f) -> f.subscribe());
        }
    }

    @SuppressWarnings("unchecked")
    public <V> String handle(String key, V e) {
        KafkaFactory<V> kafkaFactory = (KafkaFactory<V>) kafkaFactoryMap.get(e.getClass());
        kafkaFactory.submit(key, e);
        kafkaFactory.publish();
        return key;
    }
}

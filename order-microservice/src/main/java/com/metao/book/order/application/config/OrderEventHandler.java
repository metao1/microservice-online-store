package com.metao.book.order.application.config;

import com.metao.book.shared.application.kafka.KafkaFactory;
import com.metao.book.shared.application.service.StageProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventHandler {

    private final Map<Class<?>, KafkaFactory<?>> kafkaFactoryMap;

    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) {
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            kafkaFactoryMap.forEach((c, f) -> f.subscribe());
        }
    }

    @SuppressWarnings("unchecked")
    public <V> String handle(String key, V e) {
        final KafkaFactory<V> kafkaFactory = (KafkaFactory<V>) kafkaFactoryMap.get(e.getClass());
        return StageProcessor.accept(e)
            .applyExceptionally((event, err) -> {
                kafkaFactory.submit(key, event);
                kafkaFactory.publish();
                return key;
            });
    }
}

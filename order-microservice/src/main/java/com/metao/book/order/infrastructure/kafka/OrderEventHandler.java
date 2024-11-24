package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.application.config.KafkaFactory;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventHandler {

    private final KafkaFactory<OrderCreatedEvent> kafkaFactory;
    private @Value("${kafka.topic.order-created.name}") String orderTopic;

    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) {
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            kafkaFactory.subscribe();
        }
    }

    public String publishOrderCreated(OrderCreatedEvent event) {
        return StageProcessor.accept(event)
            .applyExceptionally((orderCreatedEvent, exp) -> {
                kafkaFactory.submit(orderTopic, orderCreatedEvent.getCustomerId(), orderCreatedEvent);
                kafkaFactory.publish();
                return orderCreatedEvent.getId();
            });
    }
}

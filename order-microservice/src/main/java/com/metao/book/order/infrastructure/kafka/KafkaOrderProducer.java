package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final NewTopic orderTopic;

    public void sendToKafka(OrderCreatedEvent orderCreatedEvent) {
        kafkaTemplate.send(orderTopic.name(), orderCreatedEvent.customerId(), orderCreatedEvent)
            .thenRun(() -> log.debug("Order {} sent", orderCreatedEvent));
    }
}

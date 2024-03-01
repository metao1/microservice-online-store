package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;
    private final NewTopic orderTopic;

    public void sendToKafka(OrderDTO orderEvent) {
        kafkaTemplate.send(orderTopic.name(), orderEvent.customerId(), orderEvent)
            .thenRun(() -> log.debug("Order {} sent", orderEvent));
    }
}

package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableKafka
@RequiredArgsConstructor
public class OrderKafkaListenerConfig {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @KafkaListener(
        id = "${kafka.topic.id}",
        topics = "${kafka.topic.name}",
        groupId = "${kafka.topic.group-id}"
    )
    @RetryableTopic(attempts = "1")
    public void orderKafkaListener(ConsumerRecord<String, String> orderRecord) {
        StageProcessor
            .accept(orderRecord.value())
            .map(orderMapper::toOrderCreatedEvent)
            .map(orderMapper::toEntity)
            .map(orderRepository::save)
            .acceptExceptionally(
                (entity, ex) -> log.error("saving order {}, reason: {}", entity,
                    ex != null ? ex.getMessage() : "can't process order"));
    }
}

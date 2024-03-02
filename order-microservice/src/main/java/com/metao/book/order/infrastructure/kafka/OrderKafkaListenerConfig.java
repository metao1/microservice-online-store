package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@EnableKafka
@RequiredArgsConstructor
@Transactional(KafkaConstants.KAFKA_TRANSACTION_MANAGER)
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
            .process(orderRecord.value())
            .thenApply(orderMapper::toDto)
            .thenApply(orderMapper::toEntity)
            .thenApply(orderRepository::save)
            .acceptException(
                (entity, ex) -> log.error("saving order {}, reason: {}", entity,
                    ex != null ? ex.getMessage() : "can't process order"));
    }
}

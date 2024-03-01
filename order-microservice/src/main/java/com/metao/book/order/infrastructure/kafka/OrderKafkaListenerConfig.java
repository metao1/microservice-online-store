package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@Transactional(KafkaConstants.KAFKA_TRANSACTION_MANAGER)
public class OrderKafkaListenerConfig {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @KafkaListener(
        id = "${kafka.topic.order}",
        topics = "${kafka.topic.order}",
        groupId = "order-processor-group",
        properties = {"auto.offset.reset = earliest"}
    )
    public void orderKafkaListener(ConsumerRecord<String, String> orderRecord) {
        StageProcessor
            .process(orderRecord.value())
            .thenApply(orderMapper::toDto)
            .thenApply(orderMapper::toEntity)
            .thenApply(orderRepository::save)
            .handleException((orderEntity, ex) -> {
                log.error(ex.getMessage());
                return null;
            });
    }
}

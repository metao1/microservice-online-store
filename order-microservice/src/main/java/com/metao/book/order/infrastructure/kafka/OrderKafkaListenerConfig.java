package com.metao.book.order.infrastructure.kafka;

import com.metao.book.OrderEventOuterClass.OrderEvent;
import com.metao.book.order.application.service.OrderPaymentFactory;
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
    private final OrderPaymentFactory orderMapper;

    @KafkaListener(id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}")
    @RetryableTopic(attempts = "1")
    public void onOrderEvent(ConsumerRecord<String, OrderEvent> orderRecord) {
        StageProcessor
            .accept(orderRecord.value())
            .map(orderMapper::toEntity)
            .map(orderRepository::save)
            .acceptExceptionally(
                (entity, ex) -> log.error("saving order {}, reason: {}", entity,
                    ex != null ? ex.getMessage() : "can't process order"));
    }
}

package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.domain.OrderService;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaOrderListenerConfig {

    private final OrderService orderService;

    @RetryableTopic
    @KafkaListener(id = "${kafka.topics.order-created.id}",
        topics = "${kafka.topics.order-created.name}",
        groupId = "${kafka.topics.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory")
    public void onOrderCreatedEvent(ConsumerRecord<String, OrderCreatedEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value())
            .map(KafkaOrderMapper::toEntity)
            .acceptExceptionally((entity, ex) -> {
                if (ex != null || entity == null) {
                    log.error("error while consuming order, error: {}", ex == null ? "null" : ex.getMessage());
                } else {
                    orderService.save(entity);
                    log.info("order {} saved.", entity);
                }
            });
    }

    @RetryableTopic
    @KafkaListener(id = "${kafka.topics.order-updated.id}",
        topics = "${kafka.topics.order-updated.name}",
        groupId = "${kafka.topics.order-updated.group-id}",
        containerFactory = "orderUpdatedEventKafkaListenerContainerFactory")
    public void onOrderUpdatedEvent(ConsumerRecord<String, OrderUpdatedEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value())
            .map(KafkaOrderMapper::toEntity)
            .acceptExceptionally((entity, ex) -> {
                if (ex != null || entity == null) {
                    log.error("error while consuming order, error: {}", ex == null ? "null" : ex.getMessage());
                } else {
                    orderService.save(entity);
                    log.info("order {} saved.", entity);
                }
            });
    }


}

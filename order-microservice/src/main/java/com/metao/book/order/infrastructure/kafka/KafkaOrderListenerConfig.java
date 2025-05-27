package com.metao.book.order.infrastructure.kafka;

import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.domain.OrderService;
import com.metao.book.order.domain.OrderStatus; // Added import
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
    @KafkaListener(id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory")
    public void onOrderCreatedEvent(ConsumerRecord<String, OrderCreatedEvent> orderRecord) {
        StageProcessor.accept(orderRecord.value())
            .map(KafkaOrderMapper::toEntity)
            .acceptExceptionally((entity, ex) -> {
                if (ex != null || entity == null) {
                    log.error("error while consuming order, error: {}", ex == null ? "null" : ex.getMessage());
                } else {
                    orderService.save(entity); // Initial save with NEW status (or whatever is default from mapping)
                    log.info("order {} saved.", entity);

                    // --- Start of new mock payment logic ---
                    log.info("Mock payment processing for order: {}", entity.getOrderId());
                    entity.setStatus(OrderStatus.CONFIRMED);
                    orderService.save(entity); // Save again with CONFIRMED status
                    log.info("Order {} status updated to CONFIRMED after mock payment. Notification pending.", entity.getOrderId());
                    // --- End of new mock payment logic ---
                }
            });
    }

    @RetryableTopic
    @KafkaListener(id = "${kafka.topic.order-updated.id}",
        topics = "${kafka.topic.order-updated.name}",
        groupId = "${kafka.topic.order-updated.group-id}",
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
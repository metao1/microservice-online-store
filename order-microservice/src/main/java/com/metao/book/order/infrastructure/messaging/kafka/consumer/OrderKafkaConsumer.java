package com.metao.book.order.infrastructure.messaging.kafka.consumer;

import com.metao.book.order.application.usecase.PersistOrderUseCase;
import com.metao.book.order.infrastructure.messaging.OrderCreatedEventMessage;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.architecture.InboundAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@InboundAdapter(InboundAdapter.Kind.MESSAGING)
@RequiredArgsConstructor
public class OrderKafkaConsumer {
    private final PersistOrderUseCase persistOrderUseCase;

    @KafkaListener(
        id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        if (event.getEventId().isBlank()) {
            throw new IllegalArgumentException("Order-created event ID must not be blank");
        }
        persistOrderUseCase.persistOrder(OrderCreatedEventMessage.from(event).toDomainEvent());
        acknowledgment.acknowledge();
        log.info("Persisted order-created event for order {} and acknowledged offset.", event.getId());
    }
}

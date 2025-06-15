package com.metao.book.order.infrastructure.messaging;

import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.event.DomainEventPublisher;
import com.metao.book.order.domain.model.event.OrderItemAddedEvent;
import com.metao.book.order.domain.model.event.OrderStatusChangedEvent;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.kafka.KafkaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventToKafkaEventHandler implements DomainEventPublisher {

    private final KafkaEventHandler kafkaEventHandler;

    @Override
    public void publish(DomainEvent event) {
        try {
            switch (event) {
                case com.metao.book.order.domain.model.event.OrderCreatedEvent orderCreatedEvent -> publishOrderCreatedEvent(orderCreatedEvent);
                case OrderItemAddedEvent orderItemAddedEvent -> publishOrderUpdatedEvent(orderItemAddedEvent);
                case OrderStatusChangedEvent orderStatusChangedEvent ->
                    publishOrderUpdatedEvent(orderStatusChangedEvent);
                default -> log.warn("Unknown domain event type: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Failed to publish domain event: {}", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }

    private void publishOrderCreatedEvent(com.metao.book.order.domain.model.event.OrderCreatedEvent domainEvent) {
        OrderCreatedEvent kafkaEvent = OrderCreatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setCustomerId(domainEvent.getCustomerId().getValue())
            .setStatus(com.metao.book.shared.OrderCreatedEvent.Status.NEW)
            .setCreateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();

        kafkaEventHandler.handle(domainEvent.getOrderId().value(), kafkaEvent);
        log.info("Published OrderCreatedEvent for order: {}", domainEvent.getOrderId().value());
    }

    private void publishOrderUpdatedEvent(OrderItemAddedEvent domainEvent) {
        OrderUpdatedEvent kafkaEvent = OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setProductId(domainEvent.getProductId().getValue())
            .setQuantity(domainEvent.getQuantity().getValue())
            .setPrice(domainEvent.getUnitPrice().doubleAmount().doubleValue())
            .setCurrency(domainEvent.getUnitPrice().currency().getCurrencyCode())
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();

        kafkaEventHandler.handle(domainEvent.getOrderId().value(), kafkaEvent);
        log.info("Published OrderUpdatedEvent for order: {} (item added)", domainEvent.getOrderId().value());
    }

    private void publishOrderUpdatedEvent(OrderStatusChangedEvent domainEvent) {
        OrderUpdatedEvent kafkaEvent = OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setStatus(mapOrderStatus(domainEvent.getNewStatus().name()))
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();

        kafkaEventHandler.handle(domainEvent.getOrderId().value(), kafkaEvent);
        log.info("Published OrderUpdatedEvent for order: {} (status changed from {} to {})",
            domainEvent.getOrderId().value(),
            domainEvent.getOldStatus().name(),
            domainEvent.getNewStatus().name());
    }

    private OrderUpdatedEvent.Status mapOrderStatus(String status) {
        return switch (status) {
            case "PAID" -> OrderUpdatedEvent.Status.CONFIRMED;
            case "CANCELLED" -> OrderUpdatedEvent.Status.REJECTED;
            default -> OrderUpdatedEvent.Status.NEW;
        };
    }
}

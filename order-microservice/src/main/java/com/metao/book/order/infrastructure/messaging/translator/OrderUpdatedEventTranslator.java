package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.OrderStatusChangedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedEventTranslator implements ProtobufDomainTranslator<OrderStatusChangedEvent> {

    /**
     * Translate a domain domainEvent to a protobuf message
     *
     * @param domainEvent domain domainEvent
     * @return protobuf message
     */
    @Override
    public Message translate(OrderStatusChangedEvent domainEvent) {
        return OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getEventId())
            .setStatus(mapOrderStatus(domainEvent.getNewStatus().name()))
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
            )
            .build();
    }

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    @Override
    public Class<OrderStatusChangedEvent> supports() {
        return OrderStatusChangedEvent.class;
    }

    private OrderUpdatedEvent.Status mapOrderStatus(String status) {
        return switch (status) {
            case "PAID" -> OrderUpdatedEvent.Status.CONFIRMED;
            case "CANCELLED" -> OrderUpdatedEvent.Status.REJECTED;
            default -> OrderUpdatedEvent.Status.NEW;
        };
    }
}

package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.OrderStatusChangedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedEventTranslator implements ProtobufDomainTranslator {

    @Override
    public Message translate(DomainEvent event) {
        OrderStatusChangedEvent domainEvent = (OrderStatusChangedEvent) event;
        return OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getEventId())
            .setStatus(mapOrderStatus(domainEvent.getNewStatus().name()))
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
            )
            .build();
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof OrderStatusChangedEvent;
    }

    private OrderUpdatedEvent.Status mapOrderStatus(String status) {
        return switch (status) {
            case "PAID" -> OrderUpdatedEvent.Status.CONFIRMED;
            case "CANCELLED" -> OrderUpdatedEvent.Status.REJECTED;
            default -> OrderUpdatedEvent.Status.NEW;
        };
    }
}

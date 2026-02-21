package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainOrderStatusChangedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedEventTranslator implements ProtobufDomainTranslator {

    @Override
    public Message translate(DomainEvent event) {
        DomainOrderStatusChangedEvent domainEvent = (DomainOrderStatusChangedEvent) event;
        return OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getEventId())
            .setStatus(mapOrderStatus(domainEvent.getNewStatus().name()))
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
            )
            .build();
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainOrderStatusChangedEvent;
    }

    private OrderUpdatedEvent.Status mapOrderStatus(String status) {
        return switch (status) {
            case "PAID" -> OrderUpdatedEvent.Status.CONFIRMED;
            case "CANCELLED" -> OrderUpdatedEvent.Status.REJECTED;
            default -> OrderUpdatedEvent.Status.NEW;
        };
    }
}

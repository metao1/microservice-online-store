package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainOrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.OrderUpdatedEvent.Status;
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
            .setStatus(mapOrderStatus(domainEvent.getNewStatus()))
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

    private OrderUpdatedEvent.Status mapOrderStatus(OrderStatus status) {
        return switch (status) {
            case CREATED -> Status.CREATED;
            case PENDING_PAYMENT -> Status.PENDING_PAYMENT;
            case PAID -> Status.PAID;
            case PAYMENT_FAILED -> Status.PAYMENT_FAILED;
            case PROCESSING -> Status.PROCESSING;
            case SHIPPED -> Status.SHIPPED;
            case DELIVERED -> Status.DELIVERED;
            case CANCELLED -> Status.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown status " + status);
        };
    }
}

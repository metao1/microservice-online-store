package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventTranslator implements ProtobufDomainTranslator<DomainOrderCreatedEvent> {

    @Override
    public Message translate(DomainOrderCreatedEvent domainEvent) {
        return OrderCreatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setCustomerId(domainEvent.getCustomerId().getValue())
            .setStatus(com.metao.book.shared.OrderCreatedEvent.Status.NEW)
            .setCreateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();
    }

    @Override
    public Class<DomainOrderCreatedEvent> supports() {
        return DomainOrderCreatedEvent.class;
    }
}

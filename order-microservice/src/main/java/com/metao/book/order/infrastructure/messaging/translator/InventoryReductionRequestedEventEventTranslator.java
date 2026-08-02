package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainInventoryReductionRequestedEvent;
import com.metao.book.shared.InventoryReductionRequestedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import org.springframework.stereotype.Component;

@Component
public class InventoryReductionRequestedEventEventTranslator implements ProtobufDomainEventTranslator {

    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        DomainInventoryReductionRequestedEvent domainEvent = (DomainInventoryReductionRequestedEvent) event;
        InventoryReductionRequestedEvent message = InventoryReductionRequestedEvent.newBuilder()
            .setEventId(domainEvent.getEventId().toString())
            .setOrderId(domainEvent.getOrderId().value())
            .setSku(domainEvent.getSku().value())
            .setQuantity(domainEvent.getVolume().value().doubleValue())
            .setCorrelationId(domainEvent.getOrderId().value())
            .setCausationId(domainEvent.getEventId())
            .setOccurredAt(Timestamp.newBuilder()
                .setSeconds(event.getOccurredOn().getEpochSecond())
                .setNanos(event.getOccurredOn().getNano())
                .build())
            .build();

        return new ProtobufTranslation(
            "order",
            domainEvent.getOrderId().value(),
            "inventory.reduction-requested",
            1,
            domainEvent.getOrderId().value(),
            message.toByteArray()
        );
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainInventoryReductionRequestedEvent;
    }
}

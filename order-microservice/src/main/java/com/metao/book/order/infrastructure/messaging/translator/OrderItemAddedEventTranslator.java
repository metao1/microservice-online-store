package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.OrderItemAddedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrderItemAddedEventTranslator implements ProtobufDomainTranslator<OrderItemAddedEvent> {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param domainEvent domain event
     * @return protobuf message
     */
    @Override
    public Message translate(OrderItemAddedEvent domainEvent) {
        return OrderUpdatedEvent.newBuilder()
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
    }

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    @Override
    public Class<OrderItemAddedEvent> supports() {
        return OrderItemAddedEvent.class;
    }
}

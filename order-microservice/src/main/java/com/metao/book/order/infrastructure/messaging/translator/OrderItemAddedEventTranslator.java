package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainOrderItemAddedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class OrderItemAddedEventTranslator implements ProtobufDomainTranslator {

    @Override
    public Message translate(DomainEvent event) {
        DomainOrderItemAddedEvent domainEvent = (DomainOrderItemAddedEvent) event;
        return OrderUpdatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setProductId(domainEvent.getProductSku().value())
            .setQuantity(domainEvent.getQuantity().getValue().doubleValue())
            .setPrice(domainEvent.getUnitPrice().doubleAmount().doubleValue())
            .setCurrency(domainEvent.getUnitPrice().currency().getCurrencyCode())
            .setUpdateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainOrderItemAddedEvent;
    }
}

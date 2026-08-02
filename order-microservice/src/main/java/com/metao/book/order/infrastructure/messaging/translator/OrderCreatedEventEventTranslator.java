package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventEventTranslator implements ProtobufDomainEventTranslator {

    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        DomainOrderCreatedEvent domainEvent = (DomainOrderCreatedEvent) event;

        OrderCreatedEvent.Builder builder = OrderCreatedEvent.newBuilder()
            .setId(domainEvent.getOrderId().value())
            .setEventId(domainEvent.getEventId())
            .setUserId(domainEvent.getUserId().value())
            .setStatus(OrderCreatedEvent.Status.PENDING_PAYMENT)
            .setCreateTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build());

        domainEvent.getItems().forEach(item -> builder.addItems(
            OrderCreatedEvent.OrderItem.newBuilder()
                .setSku(item.getProductSku().value())
                .setProductTitle(item.getTitle().value())
                .setQuantity(item.getQuantity().value().doubleValue())
                .setPrice(item.getUnitPrice().doubleAmount().doubleValue())
                .setCurrency(item.getUnitPrice().currency().getCurrencyCode())
                .build()));

        OrderCreatedEvent message = builder.build();
        return new ProtobufTranslation(
            "order",
            domainEvent.getOrderId().value(),
            "order.created",
            1,
            domainEvent.getOrderId().value(),
            message.toByteArray()
        );
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainOrderCreatedEvent;
    }
}

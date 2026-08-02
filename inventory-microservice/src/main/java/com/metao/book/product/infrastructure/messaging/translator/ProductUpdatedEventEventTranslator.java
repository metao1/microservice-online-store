package com.metao.book.product.infrastructure.messaging.translator;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.model.event.DomainProductUpdatedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdatedEventEventTranslator implements ProtobufDomainEventTranslator {

    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        DomainProductUpdatedEvent domainEvent = (DomainProductUpdatedEvent) event;
        ProductUpdatedEvent message = ProductUpdatedEvent.newBuilder()
            .setSku(domainEvent.getProductSku().value())
            .setUpdatedTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().getEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .setTitle(domainEvent.getTitle().value())
            .setPrice(domainEvent.getNewPrice().doubleAmount().doubleValue())
            .setCurrency(domainEvent.getNewPrice().currency().getCurrencyCode())
            .build();

        return new ProtobufTranslation(
            "product",
            domainEvent.getProductSku().value(),
            "product.updated",
            1,
            domainEvent.getProductSku().value(),
            message.toByteArray()
        );
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainProductUpdatedEvent;
    }
}

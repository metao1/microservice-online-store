package com.metao.book.product.infrastructure.messaging.translator;

import com.google.protobuf.Timestamp;
import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.domain.model.event.DomainProductCreatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedEventEventTranslator implements ProtobufDomainEventTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        DomainProductCreatedEvent domainEvent = (DomainProductCreatedEvent) event;
        Timestamp occurredOn = Timestamp.newBuilder()
            .setSeconds(domainEvent.getOccurredOn().getEpochSecond())
            .setNanos(domainEvent.getOccurredOn().getNano())
            .build();

        ProductCreatedEvent message = ProductCreatedEvent.newBuilder()
            .setSku(domainEvent.getProductSku().value())
            .setCreateTime(occurredOn)
            .setTitle(domainEvent.getTitle().value())
            .setDescription(domainEvent.getDescription().value())
            .setPrice(domainEvent.getPrice().doubleAmount().doubleValue())
            .setVolume(domainEvent.getQuantity().value().doubleValue())
            .setCurrency(domainEvent.getPrice().currency().getCurrencyCode())
            .setImageUrl(domainEvent.getImageUrl().getValue())
            .build();

        return new ProtobufTranslation(
            "product",
            domainEvent.getProductSku().value(),
            "product.created",
            1,
            domainEvent.getProductSku().value(),
            message.toByteArray()
        );
    }

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainProductCreatedEvent;
    }
}

package com.metao.book.product.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.domain.model.event.DomainProductCreatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedEventTranslator implements ProtobufDomainTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    @Override
    public Message translate(DomainEvent event) {
        DomainProductCreatedEvent domainEvent = (DomainProductCreatedEvent) event;
        Timestamp occurredOn = Timestamp.newBuilder()
            .setSeconds(domainEvent.getOccurredOn().getEpochSecond())
            .setNanos(domainEvent.getOccurredOn().getNano())
            .build();

        return ProductCreatedEvent.newBuilder()
            .setSku(domainEvent.getProductSku().value())
            .setCreateTime(occurredOn)
            .setTitle(domainEvent.getTitle().getValue())
            .setDescription(domainEvent.getDescription().getValue())
            .setPrice(domainEvent.getPrice().doubleAmount().doubleValue())
            .setVolume(domainEvent.getQuantity().getValue().doubleValue())
            .setCurrency(domainEvent.getPrice().currency().getCurrencyCode())
            .setImageUrl(domainEvent.getImageUrl().getValue())
            .build();

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

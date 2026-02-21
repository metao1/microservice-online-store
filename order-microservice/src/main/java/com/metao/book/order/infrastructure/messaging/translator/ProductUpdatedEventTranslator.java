package com.metao.book.order.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.model.event.DomainInventoryReductionRequestedEvent;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdatedEventTranslator implements ProtobufDomainTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    @Override
    public Message translate(DomainEvent event) {
        DomainInventoryReductionRequestedEvent domainEvent = (DomainInventoryReductionRequestedEvent) event;
        return ProductUpdatedEvent.newBuilder()
            .setUpdatedTime(Timestamp.newBuilder()
                .setSeconds(event.getOccurredOn().getEpochSecond())
                .setNanos(event.getOccurredOn().getNano())
                .build())
            .setSku(domainEvent.getSku().value())
            .setVolume(domainEvent.getVolume().getValue().doubleValue())
            .build();
    }

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @param event ProductUpdatedEvent
     * @return the Class object of the DomainEvent this translator supports.
     */
    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DomainInventoryReductionRequestedEvent;
    }
}

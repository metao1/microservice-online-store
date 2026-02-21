package com.metao.book.product.infrastructure.messaging.translator;

import com.google.protobuf.Message;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;

public class ProductCreatedEventTranslator implements ProtobufDomainTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    @Override
    public Message translate(DomainEvent event) {
        return null;
    }

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @param event
     * @return the Class object of the DomainEvent this translator supports.
     */
    @Override
    public boolean supports(DomainEvent event) {
        return false;
    }
}

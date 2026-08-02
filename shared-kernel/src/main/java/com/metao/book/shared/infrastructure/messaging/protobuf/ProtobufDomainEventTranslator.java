package com.metao.book.shared.infrastructure.messaging.protobuf;

import com.metao.book.shared.domain.base.DomainEvent;

public interface ProtobufDomainEventTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    ProtobufTranslation translate(DomainEvent event);

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    boolean supports(DomainEvent event);
}

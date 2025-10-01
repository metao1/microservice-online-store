package com.metao.book.shared.domain.base;

import com.google.protobuf.Message;

public interface ProtobufDomainTranslator<E extends DomainEvent> {

    /**
     * Translate a domain domainEvent to a protobuf message
     *
     * @param domainEvent domain domainEvent
     * @return protobuf message
     */
    Message translate(E domainEvent);

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    Class<E> supports();
}

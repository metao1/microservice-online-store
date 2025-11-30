package com.metao.book.shared.domain.base;

import com.google.protobuf.Message;

public interface ProtobufDomainTranslator {

    /**
     * Translate a domain event to a protobuf message
     *
     * @param event domain domain
     * @return protobuf message
     */
    Message translate(DomainEvent event);

    /**
     * Declares that a specific DomainEvent class this translator is responsible for.
     *
     * @return the Class object of the DomainEvent this translator supports.
     */
    boolean supports(DomainEvent event);
}

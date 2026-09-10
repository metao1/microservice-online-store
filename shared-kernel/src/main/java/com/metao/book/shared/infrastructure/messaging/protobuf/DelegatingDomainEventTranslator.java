package com.metao.book.shared.infrastructure.messaging.protobuf;

import com.metao.book.shared.domain.base.DomainEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DelegatingDomainEventTranslator {

    private final List<ProtobufDomainEventTranslator> translators;

    public ProtobufTranslation translate(DomainEvent event) {
        return translators.stream()
            .filter(translator -> translator.supports(event))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "No translator for " + event.getClass().getName()
            ))
            .translate(event);
    }
}

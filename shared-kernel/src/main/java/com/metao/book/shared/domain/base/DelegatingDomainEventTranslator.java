package com.metao.book.shared.domain.base;

import com.google.protobuf.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelegatingDomainEventTranslator {

    private final List<ProtobufDomainTranslator> translators;

    public <T extends DomainEvent> TranslationResult translate(T event) {
        for (ProtobufDomainTranslator translator : translators) {
            if (translator.supports(event)) {
                Message message = translator.translate(event);
                return new TranslationResult(event.getEventType(), message);
            }
        }
        throw new IllegalArgumentException("No translator found for event: " + event.getClass().getName());
    }
}

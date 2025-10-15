package com.metao.book.shared.domain.base;

import com.google.protobuf.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DelegatingDomainEventTranslator {

    private final Map<Class<?>, ProtobufDomainTranslator<?>> translatorMap = new HashMap<>();
    public DelegatingDomainEventTranslator(List<ProtobufDomainTranslator<?>> translators) {
        translators.forEach(translator -> translatorMap.put(translator.supports(), translator));
    }

    @SuppressWarnings("unchecked")
    public TranslationResult translate(DomainEvent event) {
        ProtobufDomainTranslator translator = translatorMap.get(event.getClass());
        if (translator == null) {
            throw new IllegalArgumentException("No translator found for event type: " + event.getClass().getName());
        }
        final Message translate = translator.translate(event);
        return new TranslationResult(event.getEventType(), translate);
    }
}

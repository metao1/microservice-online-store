package com.metao.book.order.infrastructure.messaging;

import com.metao.book.shared.domain.base.DelegatingDomainEventTranslator;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.DomainEventPublisher;
import com.metao.book.shared.domain.base.TranslationResult;
import com.metao.kafka.KafkaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventToKafkaEventHandler implements DomainEventPublisher {

    private final DelegatingDomainEventTranslator domainEventTranslator;
    private final KafkaEventHandler kafkaEventHandler;

    @Override
    public void publish(DomainEvent event) {
        try {
            final TranslationResult translationResult = domainEventTranslator.translate(event);
            kafkaEventHandler.send(translationResult.key(), translationResult.message());
            log.debug("Published OrderCreatedEvent for order: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish domain event: {}", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }
}

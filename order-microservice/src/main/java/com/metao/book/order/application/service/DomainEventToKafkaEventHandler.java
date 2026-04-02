package com.metao.book.order.application.service;

import com.google.protobuf.Message;
import com.metao.book.shared.domain.base.DelegatingDomainEventTranslator;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.DomainEventPublisher;
import com.metao.kafka.KafkaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventToKafkaEventHandler implements DomainEventPublisher {

    private final DelegatingDomainEventTranslator domainEventTranslator;
    private final KafkaEventHandler kafkaEventHandler;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        try {
            var translationResult = domainEventTranslator.translate(event);
            Runnable publishAction = () -> {
                var kafkaTopic = kafkaEventHandler.getKafkaTopic(translationResult.message().getClass());
                kafkaTemplate.send(kafkaTopic, translationResult.key(), translationResult.message());
                log.debug("Published domain event {} to topic {}.", event.getEventType(), kafkaTopic);
            };

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publishAction.run();
                    }
                });
                return;
            }

            publishAction.run();
        } catch (Exception e) {
            log.error("Failed to publish domain event: {}", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }
}

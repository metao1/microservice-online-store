package com.metao.book.outbox.infrastructure;

import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.shared.application.messaging.DomainEventPublisherPort;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.DelegatingDomainEventTranslator;
import lombok.RequiredArgsConstructor;

/** Stores the event with the business write, then requests post-commit dispatch. */
@RequiredArgsConstructor
public final class TransactionalOutboxDomainEventPublisherPort implements DomainEventPublisherPort {

    private final DelegatingDomainEventTranslator translator;
    private final OutboxStore outboxStore;
    private final AfterCommitOutboxDispatcher dispatcher;

    @Override
    public void publish(DomainEvent event) {
        var translation = translator.translate(event);
        outboxStore.append(new OutboxMessage(
            event.getEventId(), translation.aggregateType(), translation.aggregateId(),
            translation.eventType(), translation.schemaVersion(), translation.partitionKey(),
            translation.payload(), event.getOccurredOn(), translation.orderingKey()
        ));
        dispatcher.dispatchAfterCommit();
    }
}

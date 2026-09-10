package com.metao.book.outbox.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Schedules publication only after the transaction that stored the outbox row commits. */
@Slf4j
@RequiredArgsConstructor
public final class AfterCommitOutboxDispatcher {

    private final OutboxKafkaPublisher<?> publisher;

    public void dispatchAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publisher.publishPending();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    publisher.publishPending();
                } catch (RuntimeException exception) {
                    log.error("Outbox dispatch after commit failed; the scheduled publisher will retry", exception);
                }
            }
        });
    }
}

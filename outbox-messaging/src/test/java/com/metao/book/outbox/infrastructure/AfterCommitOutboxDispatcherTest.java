package com.metao.book.outbox.infrastructure;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AfterCommitOutboxDispatcherTest {

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void dispatchesAfterTransactionCommit() {
        OutboxKafkaPublisher publisher = Mockito.mock(OutboxKafkaPublisher.class);
        AfterCommitOutboxDispatcher dispatcher = new AfterCommitOutboxDispatcher(publisher);
        TransactionSynchronizationManager.initSynchronization();

        dispatcher.dispatchAfterCommit();

        verifyNoInteractions(publisher);
        TransactionSynchronizationManager.getSynchronizations().getFirst().afterCommit();
        verify(publisher).publishPending();
    }

    @Test
    void dispatchesImmediatelyWithoutActiveTransaction() {
        OutboxKafkaPublisher publisher = Mockito.mock(OutboxKafkaPublisher.class);
        new AfterCommitOutboxDispatcher(publisher).dispatchAfterCommit();

        verify(publisher).publishPending();
    }
}

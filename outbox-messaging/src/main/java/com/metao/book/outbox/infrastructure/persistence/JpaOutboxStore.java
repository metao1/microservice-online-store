package com.metao.book.outbox.infrastructure.persistence;

import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStatus;
import com.metao.book.outbox.application.OutboxStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaOutboxStore implements OutboxStore {

    private static final String CLAIMABLE_QUERY = """
        select event
        from JpaOutboxEntity event
        where ((event.status = :pending and event.nextAttemptAt <= :now)
           or (event.status = :inProgress and event.leaseUntil <= :now))
          and (event.orderingKey is null or not exists (
              select older.eventId
              from JpaOutboxEntity older
              where older.orderingKey = event.orderingKey
                and older.status <> :published
                and (older.occurredAt < event.occurredAt
                  or (older.occurredAt = event.occurredAt and older.eventId < event.eventId))
          ))
        order by event.occurredAt asc
        """;

    private final EntityManager entityManager;

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void append(OutboxMessage message) {
        entityManager.persist(JpaOutboxEntity.from(message));
    }

    @Override
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public List<OutboxMessage> claimPending(
        String workerId,
        int limit,
        Instant now,
        Instant leaseUntil
    ) {
        return entityManager.createQuery(CLAIMABLE_QUERY, JpaOutboxEntity.class)
            .setParameter("now", now)
            .setParameter("pending", OutboxStatus.PENDING)
            .setParameter("inProgress", OutboxStatus.IN_PROGRESS)
            .setParameter("published", OutboxStatus.PUBLISHED)
            .setMaxResults(limit)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList()
            .stream()
            .peek(entity -> {
                entity.setStatus(OutboxStatus.IN_PROGRESS);
                entity.setLeaseOwner(workerId);
                entity.setLeaseUntil(leaseUntil);
                entity.setAttemptCount(entity.getAttemptCount() + 1);
            })
            .map(JpaOutboxEntity::toMessage)
            .toList();
    }

    @Override
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void markPublished(String eventId, String workerId, Instant publishedAt) {
        withOwnedLease(eventId, workerId, entity -> {
            entity.setStatus(OutboxStatus.PUBLISHED);
            entity.setPublishedAt(publishedAt);
            entity.setLeaseOwner(null);
            entity.setLeaseUntil(null);
            entity.setLastError(null);
        });
    }

    @Override
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void rescheduleFailure(
        String eventId,
        String workerId,
        Instant nextAttemptAt,
        String errorMessage
    ) {
        withOwnedLease(eventId, workerId, entity -> {
            entity.setStatus(OutboxStatus.PENDING);
            entity.setNextAttemptAt(nextAttemptAt);
            entity.setLeaseOwner(null);
            entity.setLeaseUntil(null);
            entity.setLastError(errorMessage);
        });
    }

    @Override
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void releaseClaim(String eventId, String workerId) {
        withOwnedLease(eventId, workerId, entity -> {
            entity.setStatus(OutboxStatus.PENDING);
            entity.setLeaseOwner(null);
            entity.setLeaseUntil(null);
            entity.setAttemptCount(Math.max(0, entity.getAttemptCount() - 1));
        });
    }

    private void withOwnedLease(
        String eventId,
        String workerId,
        Consumer<JpaOutboxEntity> update
    ) {
        JpaOutboxEntity entity = entityManager.find(JpaOutboxEntity.class, eventId);
        if (entity != null && workerId.equals(entity.getLeaseOwner())) {
            update.accept(entity);
        }
    }
}

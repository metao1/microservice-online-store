package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.infrastructure.persistence.entity.ProductOutboxJpaEntity;
import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStore;
import com.metao.book.outbox.application.OutboxStatus;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProductOutboxStore implements OutboxStore {

    private final SpringDataProductOutboxRepository repository;

    @Override
    @Transactional
    public void append(OutboxMessage message) {
        repository.save(ProductOutboxJpaEntity.from(message));
    }

    @Override
    @Transactional
    public List<OutboxMessage> claimPending(
        String workerId,
        int limit,
        Instant now,
        Instant leaseUntil
    ) {
        return repository.findClaimable(
                now,
                OutboxStatus.PENDING,
                OutboxStatus.IN_PROGRESS,
                PageRequest.of(0, limit)
            )
            .stream()
            .peek(entity -> {
                entity.setStatus(OutboxStatus.IN_PROGRESS);
                entity.setLeaseOwner(workerId);
                entity.setLeaseUntil(leaseUntil);
                entity.setAttemptCount(entity.getAttemptCount() + 1);
            })
            .map(ProductOutboxJpaEntity::toMessage)
            .toList();
    }

    @Override
    @Transactional
    public void markPublished(String eventId, String workerId, Instant publishedAt) {
        repository.findById(eventId)
            .filter(entity -> workerId.equals(entity.getLeaseOwner()))
            .ifPresent(entity -> {
                entity.setStatus(OutboxStatus.PUBLISHED);
                entity.setPublishedAt(publishedAt);
                entity.setLeaseOwner(null);
                entity.setLeaseUntil(null);
                entity.setLastError(null);
            });
    }

    @Override
    @Transactional
    public void rescheduleFailure(
        String eventId,
        String workerId,
        Instant nextAttemptAt,
        String errorMessage
    ) {
        repository.findById(eventId)
            .filter(entity -> workerId.equals(entity.getLeaseOwner()))
            .ifPresent(entity -> {
                entity.setStatus(OutboxStatus.PENDING);
                entity.setNextAttemptAt(nextAttemptAt);
                entity.setLeaseOwner(null);
                entity.setLeaseUntil(null);
                entity.setLastError(errorMessage);
            });
    }
}

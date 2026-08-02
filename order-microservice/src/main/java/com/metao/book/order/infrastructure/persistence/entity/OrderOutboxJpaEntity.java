package com.metao.book.order.infrastructure.persistence.entity;

import com.metao.book.outbox.application.OutboxMessage;
import com.metao.book.outbox.application.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "domain_event_outbox")
@Getter
@Setter
@NoArgsConstructor
public class OrderOutboxJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @Column(name = "schema_version", nullable = false, updatable = false)
    private int schemaVersion;

    @Column(name = "partition_key", nullable = false, updatable = false)
    private String partitionKey;

    @Column(name = "payload", nullable = false, updatable = false)
    private byte[] payload;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OutboxStatus status;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "lease_owner", length = 255)
    private String leaseOwner;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 2_000)
    private String lastError;

    @Column(name = "published_at")
    private Instant publishedAt;

    public static OrderOutboxJpaEntity from(OutboxMessage message) {
        OrderOutboxJpaEntity entity = new OrderOutboxJpaEntity();
        entity.eventId = message.eventId();
        entity.aggregateType = message.aggregateType();
        entity.aggregateId = message.aggregateId();
        entity.eventType = message.eventType();
        entity.schemaVersion = message.schemaVersion();
        entity.partitionKey = message.partitionKey();
        entity.payload = message.payload();
        entity.occurredAt = message.occurredAt();
        entity.status = OutboxStatus.PENDING;
        entity.nextAttemptAt = message.occurredAt();
        return entity;
    }

    public OutboxMessage toMessage() {
        return new OutboxMessage(
            eventId,
            aggregateType,
            aggregateId,
            eventType,
            schemaVersion,
            partitionKey,
            payload,
            occurredAt
        );
    }
}

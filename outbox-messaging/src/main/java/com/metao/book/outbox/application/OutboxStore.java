package com.metao.book.outbox.application;

import java.time.Instant;
import java.util.List;

/** Service-local persistence port for durable outbound messages. */
public interface OutboxStore {
    void append(OutboxMessage message);

    List<OutboxMessage> claimPending(String workerId, int limit, Instant now, Instant leaseUntil);

    void markPublished(String eventId, String workerId, Instant publishedAt);

    void rescheduleFailure(String eventId, String workerId, Instant nextAttemptAt, String errorMessage);

    void releaseClaim(String eventId, String workerId);
}

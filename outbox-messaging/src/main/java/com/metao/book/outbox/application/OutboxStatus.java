package com.metao.book.outbox.application;

/** Lifecycle states for a persisted outbound integration message. */
public enum OutboxStatus {
    PENDING,
    IN_PROGRESS,
    PUBLISHED
}

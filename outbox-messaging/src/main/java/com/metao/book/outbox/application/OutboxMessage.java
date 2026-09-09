package com.metao.book.outbox.application;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/** Immutable transport-neutral representation of an event stored by one service. */
public record OutboxMessage(
    String eventId,
    String aggregateType,
    String aggregateId,
    String eventType,
    int schemaVersion,
    String partitionKey,
    byte[] payload,
    Instant occurredAt,
    String orderingKey
) {
    public OutboxMessage {
        eventId = requireText(eventId, "eventId");
        aggregateType = requireText(aggregateType, "aggregateType");
        aggregateId = requireText(aggregateId, "aggregateId");
        eventType = requireText(eventType, "eventType");
        partitionKey = requireText(partitionKey, "partitionKey");
        if (orderingKey != null) {
            orderingKey = requireText(orderingKey, "orderingKey");
        }
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be greater than or equal to 1");
        }
        payload = Arrays.copyOf(Objects.requireNonNull(payload, "payload must not be null"), payload.length);
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public OutboxMessage(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        int schemaVersion,
        String partitionKey,
        byte[] payload,
        Instant occurredAt
    ) {
        this(eventId, aggregateType, aggregateId, eventType, schemaVersion, partitionKey, payload, occurredAt, null);
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}

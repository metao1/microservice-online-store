package com.metao.book.shared.infrastructure.messaging.protobuf;

public record ProtobufTranslation(
    String aggregateType,
    String aggregateId,
    String eventType,
    int schemaVersion,
    String partitionKey,
    byte[] payload,
    String orderingKey
) {
    public ProtobufTranslation(
        String aggregateType,
        String aggregateId,
        String eventType,
        int schemaVersion,
        String partitionKey,
        byte[] payload
    ) {
        this(aggregateType, aggregateId, eventType, schemaVersion, partitionKey, payload, null);
    }
}

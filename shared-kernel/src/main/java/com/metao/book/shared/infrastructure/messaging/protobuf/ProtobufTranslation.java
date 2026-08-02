package com.metao.book.shared.infrastructure.messaging.protobuf;

public record ProtobufTranslation(
    String aggregateType,
    String aggregateId,
    String eventType,
    int schemaVersion,
    String partitionKey,
    byte[] payload
) {}
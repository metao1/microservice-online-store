package com.metao.book.outbox.infrastructure;

/** Converts a durable outbox payload into the value expected by the configured producer. */
public interface OutboxPayloadCodec<T> {

    boolean supports(String eventType, int schemaVersion);

    T deserialize(byte[] payload);

    String topic();
}

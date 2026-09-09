package com.metao.book.outbox.infrastructure;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class OutboxPayloadCodecRegistry<T> {

    private final List<OutboxPayloadCodec<T>> codecs;

    public OutboxPayloadCodec<T> get(String eventType, int schemaVersion) {
        return codecs.stream()
            .filter(codec -> codec.supports(eventType, schemaVersion))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported outbox event: %s v%d".formatted(eventType, schemaVersion)
            ));
    }
}

package com.metao.book.shared.infrastructure.messaging.protobuf;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ProtobufMessageCodecRegistry {

    private final List<ProtobufMessageCodec> codecs;

    public ProtobufMessageCodec get(String eventType, int schemaVersion) {
        return codecs.stream()
            .filter(codec -> codec.supports(eventType, schemaVersion))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported outbox event: %s v%d"
                    .formatted(eventType, schemaVersion)
            ));
    }
}

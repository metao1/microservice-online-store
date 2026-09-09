package com.metao.book.product.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.outbox.infrastructure.OutboxPayloadCodec;
import com.metao.book.shared.ProductUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdatedEventCodec implements OutboxPayloadCodec<Message> {

    private final String topic;

    public ProductUpdatedEventCodec(
        @Value("${kafka.topic.product-updated.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return eventType.equals("product.updated") && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return ProductUpdatedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid product-updated outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

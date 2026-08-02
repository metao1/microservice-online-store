package com.metao.book.product.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedEventCodec implements ProtobufMessageCodec {

    private final String topic;

    public ProductCreatedEventCodec(
        @Value("${kafka.topic.product-created.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return eventType.equals("product.created") && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return ProductCreatedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid product-created outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

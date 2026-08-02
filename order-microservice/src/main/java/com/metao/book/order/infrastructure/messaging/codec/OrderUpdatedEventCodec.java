package com.metao.book.order.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedEventCodec implements ProtobufMessageCodec {

    private final String topic;

    public OrderUpdatedEventCodec(
        @Value("${kafka.topic.order-updated.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return eventType.equals("order.status-changed") && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return OrderUpdatedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid order-updated outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

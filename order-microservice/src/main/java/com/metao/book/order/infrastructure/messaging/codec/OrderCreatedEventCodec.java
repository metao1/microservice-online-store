package com.metao.book.order.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.outbox.infrastructure.OutboxPayloadCodec;
import com.metao.book.shared.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventCodec implements OutboxPayloadCodec<Message> {

    private final String topic;

    public OrderCreatedEventCodec(
        @Value("${kafka.topic.order-created.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return eventType.equals("order.created") && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return OrderCreatedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid order-created outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

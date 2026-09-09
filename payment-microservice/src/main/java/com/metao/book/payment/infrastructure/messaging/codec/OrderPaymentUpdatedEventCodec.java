package com.metao.book.payment.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.outbox.infrastructure.OutboxPayloadCodec;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentUpdatedEventCodec implements OutboxPayloadCodec<Message> {

    private final String topic;

    public OrderPaymentUpdatedEventCodec(
        @Value("${kafka.topic.order-payment.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return (eventType.equals("payment.processed") || eventType.equals("payment.failed"))
            && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return OrderPaymentUpdatedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid payment-update outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

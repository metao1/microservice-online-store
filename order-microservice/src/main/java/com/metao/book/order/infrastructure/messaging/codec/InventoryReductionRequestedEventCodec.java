package com.metao.book.order.infrastructure.messaging.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.metao.book.shared.InventoryReductionRequestedEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufMessageCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryReductionRequestedEventCodec implements ProtobufMessageCodec {

    private final String topic;

    public InventoryReductionRequestedEventCodec(
        @Value("${kafka.topic.inventory-reduction-requested.name}") String topic
    ) {
        this.topic = topic;
    }

    @Override
    public boolean supports(String eventType, int schemaVersion) {
        return eventType.equals("inventory.reduction-requested") && schemaVersion == 1;
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return InventoryReductionRequestedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException ex) {
            throw new IllegalArgumentException("Invalid inventory-reduction outbox payload", ex);
        }
    }

    @Override
    public String topic() {
        return topic;
    }
}

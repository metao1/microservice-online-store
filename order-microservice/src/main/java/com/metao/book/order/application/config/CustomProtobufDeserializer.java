package com.metao.book.order.application.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.metao.book.order.OrderCreatedEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class CustomProtobufDeserializer implements Deserializer<OrderCreatedEvent> {

    @Override
    public OrderCreatedEvent deserialize(String topic, byte[] data) {
        try {
            return OrderCreatedEvent.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
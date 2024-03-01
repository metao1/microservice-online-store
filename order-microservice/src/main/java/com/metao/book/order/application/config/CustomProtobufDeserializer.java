package com.metao.book.order.application.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.metao.book.OrderEventOuterClass.OrderEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class CustomProtobufDeserializer implements Deserializer<OrderEvent> {

    @Override
    public OrderEvent deserialize(String topic, byte[] data) {
        try {
            return OrderEvent.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
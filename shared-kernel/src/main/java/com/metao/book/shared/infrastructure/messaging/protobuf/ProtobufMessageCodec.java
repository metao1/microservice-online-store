package com.metao.book.shared.infrastructure.messaging.protobuf;

import com.google.protobuf.Message;

public interface ProtobufMessageCodec {

    boolean supports(String eventType, int schemaVersion);

    Message deserialize(byte[] payload);

    String topic();
}
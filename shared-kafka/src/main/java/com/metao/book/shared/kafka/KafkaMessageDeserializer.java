package com.metao.book.shared.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public class KafkaMessageDeserializer implements CommonErrorHandler {

    @Override
    public void handleOtherException(
        Exception thrownException,
        Consumer<?, ?> consumer,
        MessageListenerContainer container,
        boolean batchListener
    ) {
        manageException(thrownException, consumer);
    }

    private void manageException(Exception ex, Consumer<?, ?> consumer) {
        log.error("Error polling message: " + ex.getMessage());
        if (ex instanceof RecordDeserializationException rde) {
            consumer.seek(rde.topicPartition(), rde.offset() + 1L);
            consumer.commitSync();
        } else {
            log.error("Exception not handled");
        }
    }
}

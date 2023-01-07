package com.metao.book.shared.kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.stereotype.Component;

@Component
public class StreamDeserializationErrorHandler implements DeserializationExceptionHandler {

    private int errorCounter = 0;

    /**
     * Inspect a record and the exception received.
     * <p>
     * Note, that the passed in {@link ProcessorContext} only allows to access
     * metadata like the task ID. However, it
     * cannot be used to emit records via
     * {@link ProcessorContext#forward(Object, Object)}; calling {@code forward()}
     * (and some other methods) would result in a runtime exception.
     *
     * @param context   processor context
     * @param record    record that failed deserialization
     * @param exception the actual exception
     */
    @Override
    public DeserializationHandlerResponse handle(
            ProcessorContext context,
            ConsumerRecord<byte[], byte[]> record,
            Exception exception) {
        if (errorCounter++ < 25) {
            return DeserializationHandlerResponse.CONTINUE;
        }
        return DeserializationHandlerResponse.FAIL;
    }

    /**
     * Configure this class with the given key-value pairs
     */
    @Override
    public void configure(Map<String, ?> configs) {

    }
}
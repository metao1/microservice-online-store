package com.metao.book.order.application.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;

class IgnoreRecordTooLargeHandler implements ProductionExceptionHandler {

    @Override
    public void configure(Map<String, ?> config) {
    }

    public ProductionExceptionHandlerResponse handle(
        final ProducerRecord<byte[], byte[]> record,
        final Exception exception
    ) {
        if (exception instanceof RecordTooLargeException) {
            return ProductionExceptionHandlerResponse.CONTINUE;
        } else {
            return ProductionExceptionHandlerResponse.FAIL;
        }
    }

}

package com.metao.book.order.application.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class StreamRecordProducerErrorHandler implements ProductionExceptionHandler {

        /**
         * Inspect a record that we attempted to produce, and the exception that resulted from attempting to produce it
         * and determine whether or not to continue processing.
         *
         * @param record    The record that failed to produce
         * @param exception The exception that occurred during production
         */
        @Override
        public ProductionExceptionHandlerResponse handle(
            ProducerRecord<byte[], byte[]> record, Exception exception
        ) {
            if (exception instanceof RecordTooLargeException) {
                return ProductionExceptionHandlerResponse.CONTINUE;
            }
            return ProductionExceptionHandlerResponse.FAIL;
        }

        /**
         * Configure this class with the given key-value pairs
         */
        @Override
        public void configure(Map<String, ?> configs) {

        }
    }
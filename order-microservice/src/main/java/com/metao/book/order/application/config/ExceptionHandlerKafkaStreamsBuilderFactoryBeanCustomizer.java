package com.metao.book.order.application.config;

import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionHandlerKafkaStreamsBuilderFactoryBeanCustomizer implements StreamsUncaughtExceptionHandler {

    /**
     * Inspect the exception received in a stream thread and respond with an action.
     *
     * @param exception the actual exception
     */
    @Override
    public StreamThreadExceptionResponse handle(Throwable exception) {
        log.warn(exception.getMessage());
        return REPLACE_THREAD;
    }
}

package com.metao.book.shared.kafka;

import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class StreamCustomExceptionHandler implements StreamsUncaughtExceptionHandler {

    /**
     * Inspect the exception received in a stream thread and respond with an action.
     *
     * @param exception the actual exception
     */
    @Override
    public StreamThreadExceptionResponse handle(Throwable exception) {
        if (exception instanceof StreamsException) {
            var cause = exception.getCause();
            if (cause.getMessage().equals("Retryable transient error")) {
                return StreamThreadExceptionResponse.REPLACE_THREAD;
            }
        }
        return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
    }
}
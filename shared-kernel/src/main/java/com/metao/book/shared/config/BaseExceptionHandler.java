package com.metao.book.shared.config;

import com.metao.book.shared.rest.client.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Base exception handler with common exception handling logic.
 * Subclass this in each microservice and add service-specific handlers.
 */
@Slf4j
public abstract class BaseExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var message = String.format("Message is not readable: %s", ex.getMessage());
        log.error(message, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    protected ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
        return ResponseEntity
            .status(status)
            .body(new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                null,
                null,
                Instant.now()
            ));
    }
}

package com.metao.book.product.application.config;

import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.exception.IdempotencyKeyConflictException;
import com.metao.book.shared.rest.client.ApiError;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
        {
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class,
            IdempotencyKeyConflictException.class,
            ProductNotFoundException.class
        }
    )
    public ResponseEntity<ApiError> handleException(Exception ex) {
        HttpStatus status;
        String message = ex.getMessage();

        switch (ex) {
            case HttpMessageNotReadableException httpEx -> {
                status = HttpStatus.BAD_REQUEST;
                message = String.format("Message is not readable for [%s] [%s]", httpEx.getMessage(),
                    httpEx.getHttpInputMessage());
                log.error(httpEx.getMessage(), message);
            }
            case IllegalArgumentException illegalEx -> {
                status = HttpStatus.BAD_REQUEST;
                log.error(illegalEx.getMessage(), illegalEx);
            }
            case IdempotencyKeyConflictException conflictException -> {
                status = HttpStatus.CONFLICT;
                log.warn(conflictException.getMessage());
            }
            case ProductNotFoundException notFoundEx -> {
                status = HttpStatus.NOT_FOUND;
                log.error(notFoundEx.getMessage(), notFoundEx);
            }
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                log.error(ex.getMessage(), ex);
            }
        }

        ApiError error = new ApiError(
            status.value(),
            status.getReasonPhrase(),
            message,
            null,
            null,
            Instant.now()
        );
        return ResponseEntity.status(status).body(error);
    }
}

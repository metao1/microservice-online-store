package com.metao.book.order.application.config;

import com.metao.book.order.domain.exception.ShoppingCartIsEmptyException;
import com.metao.book.shared.rest.client.ApiError;
import java.time.Instant;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleException(HttpMessageNotReadableException ex) {
        var message = String.format("Message is not readable for [%s] [%s]", ex.getMessage(), ex.getHttpInputMessage());
        log.error(ex.getMessage(), message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                null,
                null,
                Instant.now()
            ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleException(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    ex.getMessage(),
                    null,
                    null,
                    Instant.now()
                )
            );
    }

    @ExceptionHandler(ShoppingCartIsEmptyException.class)
    public ResponseEntity<ApiError> handleException(ShoppingCartIsEmptyException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiError(
                    HttpStatus.CONFLICT.value(),
                    HttpStatus.CONFLICT.getReasonPhrase(),
                    ex.getMessage(),
                    null,
                    null,
                    Instant.now()
                )
            );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleException(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    ex.getMessage(),
                    null,
                    null,
                    Instant.now()
                )
            );
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleException(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiError(
                    HttpStatus.CONFLICT.value(),
                    HttpStatus.CONFLICT.getReasonPhrase(),
                    "Resource was updated concurrently. Please retry.",
                    null,
                    null,
                    Instant.now()
                )
            );
    }
}

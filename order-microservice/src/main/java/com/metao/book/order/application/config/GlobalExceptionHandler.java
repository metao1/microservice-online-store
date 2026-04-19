package com.metao.book.order.application.config;

import com.metao.book.order.domain.exception.ShoppingCartIsEmptyException;
import com.metao.book.shared.config.BaseExceptionHandler;
import com.metao.book.shared.rest.client.ApiError;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ShoppingCartIsEmptyException.class)
    public ResponseEntity<ApiError> handleShoppingCartEmpty(ShoppingCartIsEmptyException ex) {
        log.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleOptimisticLock(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Resource was updated concurrently. Please retry.");
    }
}

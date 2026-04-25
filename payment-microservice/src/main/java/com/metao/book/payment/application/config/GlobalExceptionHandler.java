package com.metao.book.payment.application.config;

import com.metao.book.payment.domain.exception.DuplicatePaymentException;
import com.metao.book.payment.domain.exception.PaymentNotFoundException;
import com.metao.book.shared.config.BaseExceptionHandler;
import com.metao.book.shared.rest.client.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiError> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ApiError> handleDuplicatePayment(DuplicatePaymentException ex) {
        log.warn(ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        log.warn("Optimistic locking conflict while updating payment", ex);
        return buildResponse(HttpStatus.CONFLICT, "Payment was modified concurrently. Please retry.");
    }
}

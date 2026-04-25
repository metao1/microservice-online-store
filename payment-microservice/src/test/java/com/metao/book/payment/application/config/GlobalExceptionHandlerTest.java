package com.metao.book.payment.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.shared.rest.client.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOptimisticLockingFailure_shouldReturnConflict() {
        // Given
        OptimisticLockingFailureException exception = new OptimisticLockingFailureException("stale object");

        // When
        ResponseEntity<ApiError> response = handler.handleOptimisticLockingFailure(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Payment was modified concurrently. Please retry.");
    }
}

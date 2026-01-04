package com.metao.book.shared.rest.client;

import org.springframework.validation.FieldError;
import java.time.Instant;
import java.util.Set;

public record ApiError(
    /*
     * HTTP status code.
     */
    int status,
    /*
     * Reason phrase for the HTTP status code.
     */
    String reason,
    /*
     * A user-friendly message about the error.
     */
    String message,

    String errorCode,

    /*
     * Holds a user-friendly detailed message about the error.
     */
    Set<FieldError> details,

    /*
     * The date-time instance of when the error happened.
     */
    Instant timestamp
) {

}
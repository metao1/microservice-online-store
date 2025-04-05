package com.metao.book.product.application.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Reason phrase for the HTTP status code.
     */
    private String reason;

    /**
     * A user-friendly message about the error.
     */
    private String message;

    private String errorCode;

    /**
     * Holds a user-friendly detailed message about the error.
     */
    private Set<FieldError> details = new HashSet<>();

    /**
     * The date-time instance of when the error happened.
     */
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    @Builder
    public ApiError(HttpStatus status, final String message, String errorCode) {
        this();
        this.status = status.value();
        this.reason = status.getReasonPhrase();
        this.message = message;
        this.errorCode = errorCode;
    }

}
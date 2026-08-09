package com.metao.book.shared.security;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for security-related exceptions.
 *
 * <p>Translates Spring Security exceptions into appropriate HTTP responses:
 * <ul>
 *   <li>Authentication failures -> 401 Unauthorized</li>
 *   <li>Authorization failures -> 403 Forbidden</li>
 *   <li>JWT validation errors -> 401 Unauthorized</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * Handles authentication failures (invalid credentials, missing token, etc.).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "Authentication required. Please provide a valid JWT token."
        );
    }

    /**
     * Handles bad credentials specifically.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex) {
        log.debug("Bad credentials: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "BAD_CREDENTIALS",
            "Invalid username or password."
        );
    }

    /**
     * Handles JWT-specific exceptions (invalid signature, expired token, etc.).
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(
            JwtException ex) {
        log.debug("JWT validation failed: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "INVALID_TOKEN",
            "The provided JWT token is invalid or expired."
        );
    }

    /**
     * Handles authorization failures (insufficient privileges).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "You do not have permission to access this resource."
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message) {
        return ResponseEntity
            .status(status)
            .body(Map.of(
                "status", status.value(),
                "error", errorCode,
                "message", message
            ));
    }
}

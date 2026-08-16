package com.metao.book.shared.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class to access the current authenticated user's information from JWT.
 *
 * <p>This class provides a safe abstraction for retrieving the authenticated
 * subject (user ID) from the JWT token in controller/service layers.
 *
 * <p>Usage example:
 * <pre>{@code
 * String userId = CurrentUser.subject();
 * }</pre>
 */
public final class CurrentUser {

    private CurrentUser() {
        // Prevent instantiation
    }

    /**
     * Retrieves the subject (sub claim) from the current authenticated JWT token.
     *
     * @return the user's subject identifier from the JWT
     * @throws IllegalStateException if no authentication is present or if the
     *         authentication is not a JWT-based token
     */
    public static String subject() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in security context");
        }

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }

        throw new IllegalStateException(
            "Expected JwtAuthenticationToken but got: " + authentication.getClass().getName()
        );
    }

    /**
     * Retrieves the raw JWT token from the current authentication.
     *
     * @return the current JWT token
     * @throws IllegalStateException if no authentication is present or if the
     *         authentication is not a JWT-based token
     */
    public static Jwt token() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in security context");
        }

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }

        throw new IllegalStateException(
            "Expected JwtAuthenticationToken but got: " + authentication.getClass().getName()
        );
    }

    /**
     * Checks if the current user has a specific role.
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * Checks if the current user has a specific scope.
     *
     * @param scope the scope to check (without SCOPE_ prefix)
     * @return true if the user has the scope, false otherwise
     */
    public static boolean hasScope(String scope) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String scopeWithPrefix = scope.startsWith("SCOPE_") ? scope : "SCOPE_" + scope;
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(scopeWithPrefix));
    }
}

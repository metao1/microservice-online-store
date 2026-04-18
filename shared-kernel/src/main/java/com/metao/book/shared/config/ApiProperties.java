package com.metao.book.shared.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the public HTTP API exposed by each microservice.
 *
 * <p>Controls the API version advertised by OpenAPI / documentation tooling and
 * the CORS policy applied to inbound requests. Values are environment-configurable
 * so that production deployments can enforce an explicit allow-list of origins
 * rather than relying on wildcards.
 *
 * <p>All settings are bound under the {@code app.api} prefix, e.g.
 *
 * <pre>
 * app:
 *   api:
 *     version: 1.0
 *     cors:
 *       enabled: true
 *       allowed-origins:
 *         - https://example.com
 *       allow-credentials: true
 * </pre>
 */
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    /**
     * Semantic API version advertised through OpenAPI / documentation endpoints
     * and any custom response headers. Kept as a property so it can be bumped
     * without rebuilding the application.
     */
    private String version = "1.0";

    private final Cors cors = new Cors();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Cors getCors() {
        return cors;
    }

    /**
     * CORS policy settings. Defaults are intentionally locked down — CORS is
     * disabled until explicitly enabled, and there is no wildcard allow-list.
     */
    public static class Cors {

        /** Whether a CORS mapping is registered at all. */
        private boolean enabled = false;

        /** Path pattern the CORS mapping is applied to. */
        private String pathPattern = "/**";

        /**
         * Exact origins allowed (e.g. {@code https://app.example.com}). Safe to
         * combine with {@link #allowCredentials}. Empty by default.
         */
        private List<String> allowedOrigins = new ArrayList<>();

        /**
         * Origin patterns that may contain wildcards (e.g. {@code https://*.example.com}).
         * Spring requires this — not {@link #allowedOrigins} — whenever an origin
         * needs wildcard matching in combination with credentials.
         */
        private List<String> allowedOriginPatterns = new ArrayList<>();

        private List<String> allowedMethods = new ArrayList<>(
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        private List<String> allowedHeaders = new ArrayList<>(Arrays.asList("*"));

        private List<String> exposedHeaders = new ArrayList<>();

        /**
         * Whether cookies / {@code Authorization} headers may be sent. Off by
         * default — only turn on when the origin list is explicit.
         */
        private boolean allowCredentials = false;

        private Duration maxAge = Duration.ofMinutes(30);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPathPattern() {
            return pathPattern;
        }

        public void setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public Duration getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
        }
    }
}

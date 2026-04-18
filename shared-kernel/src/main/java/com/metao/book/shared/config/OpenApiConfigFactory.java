package com.metao.book.shared.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Shared configuration for the public HTTP API of each microservice.
 *
 * <p>Centralises two concerns that were previously duplicated (and insecure) in
 * every service's own {@code WebConfig}:
 *
 * <ul>
 *   <li><b>CORS policy</b> — driven entirely by {@link ApiProperties.Cors}. There
 *       is no hardcoded wildcard + {@code allowCredentials=true} combination
 *       (which Spring rejects at runtime anyway). Each environment opts in to
 *       the specific origins it trusts, via {@code app.api.cors.*} properties
 *       or their associated environment variables.</li>
 *   <li><b>API version</b> — sourced from {@code app.api.version} so it is
 *       configurable per environment instead of being hardcoded in source.</li>
 * </ul>
 *
 * <p>CORS is disabled by default: services must explicitly set
 * {@code app.api.cors.enabled=true} and provide an allow-list, which prevents
 * an accidental "allow any origin with credentials" deployment.
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties(ApiProperties.class)
public class OpenApiConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(OpenApiConfigFactory.class);

    /**
     * Exposes the configured API version as a bean so that downstream components
     * (OpenAPI definitions, {@code /info} endpoints, custom response headers,
     * etc.) can consume it without re-reading the property.
     */
    @Bean("apiVersion")
    @ConditionalOnMissingBean(name = "apiVersion")
    public String apiVersion(ApiProperties properties) {
        return properties.getVersion();
    }

    /**
     * Registers the CORS mapping only when {@code app.api.cors.enabled=true}.
     * When disabled (the default) no CORS headers are added by this factory,
     * avoiding the previous pattern where every service silently accepted
     * credentialed requests from any origin.
     */
    @Bean("apiCorsConfigurer")
    @ConditionalOnMissingBean(name = "apiCorsConfigurer")
    @ConditionalOnProperty(prefix = "app.api.cors", name = "enabled", havingValue = "true")
    public WebMvcConfigurer apiCorsConfigurer(ApiProperties properties) {
        ApiProperties.Cors cors = properties.getCors();
        validate(cors);

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var registration = registry.addMapping(cors.getPathPattern())
                    .allowedMethods(toArray(cors.getAllowedMethods()))
                    .allowedHeaders(toArray(cors.getAllowedHeaders()))
                    .allowCredentials(cors.isAllowCredentials())
                    .maxAge(cors.getMaxAge().toSeconds());

                if (!cors.getAllowedOrigins().isEmpty()) {
                    registration.allowedOrigins(toArray(cors.getAllowedOrigins()));
                }
                if (!cors.getAllowedOriginPatterns().isEmpty()) {
                    registration.allowedOriginPatterns(toArray(cors.getAllowedOriginPatterns()));
                }
                if (!cors.getExposedHeaders().isEmpty()) {
                    registration.exposedHeaders(toArray(cors.getExposedHeaders()));
                }
            }
        };
    }

    /**
     * Fails fast on the specific misconfiguration this factory was created to
     * replace: {@code allowedOrigins=["*"]} together with
     * {@code allowCredentials=true}. Spring would otherwise throw a less
     * obvious error at request time.
     */
    private static void validate(ApiProperties.Cors cors) {
        if (cors.getAllowedOrigins().isEmpty() && cors.getAllowedOriginPatterns().isEmpty()) {
            log.warn("app.api.cors.enabled=true but no allowed origins or origin patterns are "
                + "configured; CORS preflight requests will be rejected.");
        }
        if (cors.isAllowCredentials() && cors.getAllowedOrigins().contains("*")) {
            throw new IllegalStateException(
                "Insecure CORS configuration: allowedOrigins=\"*\" cannot be combined with "
                    + "allowCredentials=true. Use app.api.cors.allowed-origin-patterns or list "
                    + "exact origins under app.api.cors.allowed-origins.");
        }
    }

    private static String[] toArray(List<String> values) {
        return values.toArray(new String[0]);
    }
}

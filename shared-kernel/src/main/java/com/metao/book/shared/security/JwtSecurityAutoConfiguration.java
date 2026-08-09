package com.metao.book.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Auto-configuration for JWT-based OAuth2 Resource Server security.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Enables OAuth2 Resource Server with JWT validation</li>
 *   <li>Validates issuer and audience claims</li>
 *   <li>Maps JWT scopes/roles to Spring Security authorities</li>
 *   <li>Configures stateless session management</li>
 *   <li>Permits only health/readiness/liveness endpoints without authentication</li>
 *   <li>Requires authentication for all other endpoints</li>
 * </ul>
 *
 * <p>Configuration properties (via {@link JwtSecurityProperties}):
 * <ul>
 *   <li>{@code app.security.jwt.issuer-uri} - OIDC issuer URL (required)</li>
 *   <li>{@code app.security.jwt.audience} - Expected audience claim (optional)</li>
 *   <li>{@code app.security.jwt.enabled} - Enable/disable security (default: true)</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass({ HttpSecurity.class, Jwt.class })
@EnableConfigurationProperties(JwtSecurityProperties.class)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class JwtSecurityAutoConfiguration {

    /**
     * Configures the security filter chain for OAuth2 Resource Server with JWT.
     */
    @Bean
    @ConditionalOnMissingBean(name = "resourceServerSecurityFilterChain")
    @ConditionalOnProperty(prefix = "app.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain resourceServerSecurityFilterChain(
            HttpSecurity http,
            HandlerMappingIntrospector introspector,
            JwtSecurityProperties properties,
            Converter<Jwt, ? extends Collection<? extends GrantedAuthority>> jwtAuthoritiesConverter) throws Exception {

        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(mvcMatcherBuilder.pattern("/actuator/health")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/actuator/health/**")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/actuator/liveness")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/actuator/readiness")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/*/health")).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter(properties, jwtAuthoritiesConverter))
                )
            );

        // Configure JWT issuer validation
        http.oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwkSetUri(properties.getIssuerUri() + "/protocol/openid-connect/certs")
            )
        );

        return http.build();
    }

    /**
     * Creates the JWT authentication converter that maps JWT claims to Spring Security authorities.
     */
    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, ? extends Collection<? extends GrantedAuthority>> jwtAuthoritiesConverter() {
        return jwt -> {
            List<String> roles = Optional.ofNullable((List<String>) jwt.getClaim("roles"))
                .orElse(List.of());

            List<String> scopes = Optional.ofNullable(jwt.getClaimAsStringList("scope"))
                .orElse(List.of());

            // Map roles to ROLE_ prefix
            List<GrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            // Map scopes to SCOPE_ prefix
            List<GrantedAuthority> scopeAuthorities = scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());

            roleAuthorities.addAll(scopeAuthorities);
            return roleAuthorities;
        };
    }

    /**
     * Creates the JWT authentication converter with audience validation.
     */
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
            JwtSecurityProperties properties,
            Converter<Jwt, ? extends Collection<? extends GrantedAuthority>> authoritiesConverter) {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        // Set principal claim name to 'sub' (subject)
        converter.setPrincipalClaimName("sub");

        return jwt -> {
            // Validate audience if configured
            if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
                List<String> aud = jwt.getAudience();
                if (aud == null || !aud.contains(properties.getAudience())) {
                    throw new IllegalStateException(
                        "Invalid JWT audience. Expected: " + properties.getAudience() +
                        ", got: " + aud
                    );
                }
            }

            Collection<? extends GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }
}

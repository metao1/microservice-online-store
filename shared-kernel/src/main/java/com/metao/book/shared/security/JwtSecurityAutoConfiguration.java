package com.metao.book.shared.security;

import java.util.Collection;
import java.util.List;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for JWT-based OAuth2 Resource Server security.
 */
@AutoConfiguration
@ConditionalOnClass({ HttpSecurity.class, Jwt.class })
@EnableConfigurationProperties(JwtSecurityProperties.class)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class JwtSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "resourceServerSecurityFilterChain")
    @ConditionalOnProperty(prefix = "app.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain resourceServerSecurityFilterChain(
        HttpSecurity http,
        JwtSecurityProperties properties,
        Converter<Jwt, Collection<GrantedAuthority>> jwtAuthoritiesConverter,
        JwtDecoder jwtDecoder
    ) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health",
                    "/actuator/health/**",
                    "/actuator/liveness",
                    "/actuator/readiness",
                    "/api/*/health")
                .permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter(properties, jwtAuthoritiesConverter))
                )
            );

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtAuthoritiesConverter() {
        return new JwtAuthoritiesConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "app.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder(JwtSecurityProperties properties) {
        return NimbusJwtDecoder.withJwkSetUri(properties.getIssuerUri() + "/protocol/openid-connect/certs")
            .build();
    }

    private static final class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<String> roles = Optional.ofNullable((List<String>) jwt.getClaim("roles"))
                .orElse(List.of());

            List<String> scopes = Optional.ofNullable(jwt.getClaimAsStringList("scope"))
                .orElse(List.of());

            List<GrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            List<GrantedAuthority> scopeAuthorities = scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());

            roleAuthorities.addAll(scopeAuthorities);
            return roleAuthorities;
        }
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
        JwtSecurityProperties properties,
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter
    ) {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        converter.setPrincipalClaimName("sub");

        return jwt -> {
            if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
                List<String> aud = jwt.getAudience();
                if (aud == null || !aud.contains(properties.getAudience())) {
                    throw new IllegalStateException(
                        "Invalid JWT audience. Expected: " + properties.getAudience() +
                            ", got: " + aud
                    );
                }
            }

            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }
}

package com.metao.book.shared.security;

import java.util.Collection;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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
        Converter<Jwt, Collection<GrantedAuthority>> jwtAuthoritiesConverter,
        JwtDecoder jwtDecoder
    ) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health",
                    "/actuator/health/**",
                    "/actuator/liveness",
                    "/actuator/readiness",
                    "/api/*/health")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**")
                .permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter(jwtAuthoritiesConverter))
                )
            );

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtAuthoritiesConverter() {
        return new KeycloakJwtAuthoritiesConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "app.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder(JwtSecurityProperties properties) {
        NimbusJwtDecoder decoder = properties.getJwkSetUri() == null || properties.getJwkSetUri().isBlank()
            ? JwtDecoders.fromIssuerLocation(properties.getIssuerUri())
            : NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(properties.getIssuerUri()),
            new AudienceValidator(properties.getAudience())
        ));
        return decoder;
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter
    ) {
        return jwt -> {
            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }
}

package com.metao.book.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtAuthoritiesConverterTest {

    private final KeycloakJwtAuthoritiesConverter converter = new KeycloakJwtAuthoritiesConverter();

    @Test
    void extractsRealmRolesAndSpaceDelimitedScopeAuthorities() {
        Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("CUSTOMER")), "scope", "orders:read cart:write"));

        assertThat(converter.convert(jwt))
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_CUSTOMER", "SCOPE_orders:read", "SCOPE_cart:write");
    }

    @Test
    void mergesCompatibleTopLevelRolesWithoutDuplicatingAuthorities() {
        Jwt jwt = jwt(Map.of(
            "realm_access", Map.of("roles", List.of("CUSTOMER", "ADMIN")),
            "roles", List.of("ADMIN", "SUPPORT")
        ));

        assertThat(converter.convert(jwt))
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_SUPPORT");
    }

    @Test
    void ignoresMalformedRoleAndScopeClaims() {
        Jwt jwt = jwt(Map.of("realm_access", "not-a-map", "roles", "not-a-list", "scope", List.of("not-a-string")));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    private Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuedAt(Instant.parse("2026-08-15T00:00:00Z"))
            .expiresAt(Instant.parse("2026-08-15T01:00:00Z"));
        claims.forEach(builder::claim);
        return builder.build();
    }
}

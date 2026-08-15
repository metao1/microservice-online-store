package com.metao.book.shared.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Converts Keycloak realm roles, compatible legacy roles, and OAuth scopes into authorities. */
public final class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> authorities = new LinkedHashSet<>();
        addRoles(authorities, realmRoles(jwt));
        addRoles(authorities, stringListClaim(jwt, "roles"));
        addScopes(authorities, jwt.getClaim("scope"));

        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    private Collection<String> realmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return Set.of();
        }
        return strings(realmAccessMap.get("roles"));
    }

    private Collection<String> stringListClaim(Jwt jwt, String claimName) {
        return strings(jwt.getClaim(claimName));
    }

    private Collection<String> strings(Object value) {
        if (!(value instanceof Collection<?> values)) {
            return Set.of();
        }
        return values.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(role -> !role.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void addRoles(Set<String> authorities, Collection<String> roles) {
        roles.forEach(role -> authorities.add("ROLE_" + role));
    }

    private void addScopes(Set<String> authorities, Object scopeClaim) {
        if (!(scopeClaim instanceof String scopes)) {
            return;
        }
        for (String scope : scopes.trim().split("\\s+")) {
            if (!scope.isBlank()) {
                authorities.add("SCOPE_" + scope);
            }
        }
    }
}

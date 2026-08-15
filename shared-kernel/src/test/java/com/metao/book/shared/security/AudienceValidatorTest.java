package com.metao.book.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    @Test
    void acceptsTokenContainingConfiguredAudience() {
        Jwt jwt = jwtWithAudience(List.of("account", "bookstore-api"));

        assertThat(new AudienceValidator("bookstore-api").validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenMissingConfiguredAudience() {
        Jwt jwt = jwtWithAudience(List.of("account"));

        var result = new AudienceValidator("bookstore-api").validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
            .extracting(OAuth2Error::getErrorCode)
            .containsExactly("invalid_token");
    }

    @Test
    void rejectsTokenWithoutAudienceClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuedAt(Instant.parse("2026-08-15T00:00:00Z"))
            .expiresAt(Instant.parse("2026-08-15T01:00:00Z"))
            .build();

        var result = new AudienceValidator("bookstore-api").validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
            .extracting(error -> error.getErrorCode())
            .containsExactly("invalid_token");
    }

    @Test
    void acceptsTokenWhenAudienceValidationIsDisabled() {
        Jwt jwt = jwtWithAudience(List.of("account"));

        assertThat(new AudienceValidator(" ").validate(jwt).hasErrors()).isFalse();
    }

    private Jwt jwtWithAudience(List<String> audience) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .audience(audience)
            .issuedAt(Instant.parse("2026-08-15T00:00:00Z"))
            .expiresAt(Instant.parse("2026-08-15T01:00:00Z"))
            .build();
    }
}

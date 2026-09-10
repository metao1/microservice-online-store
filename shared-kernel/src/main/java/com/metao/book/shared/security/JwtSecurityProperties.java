package com.metao.book.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT security.
 *
 * <p>Prefix: {@code app.security.jwt}
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtSecurityProperties {

    /**
     * OIDC issuer URI (e.g., http://localhost:8080/realms/myrealm).
     * Required when security is enabled.
     */
    private String issuerUri;

    /**
     * Optional JWK Set URI used by services that need to fetch keys through an internal network.
     */
    private String jwkSetUri;

    /**
     * Expected audience claim value for token validation.
     * Optional - if not set, audience validation is skipped.
     */
    private String audience;

    /**
     * Enable JWT security validation.
     * Default: true
     */
    private boolean enabled = true;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

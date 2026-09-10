package com.coditramuntana.musicrecords.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of the JWT support, bound from the {@code app.security.jwt} prefix.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * Base64 encoded signing key. Must decode to at least 256 bits for HS256.
     */
    private String secret;

    /**
     * Lifetime of an access token, in milliseconds.
     */
    private long accessTokenExpirationMs;

    /**
     * Lifetime of a refresh token, in milliseconds.
     */
    private long refreshTokenExpirationMs;

    /**
     * Value of the {@code iss} claim.
     */
    private String issuer = "music-records";

}

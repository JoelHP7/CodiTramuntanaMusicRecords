package com.coditramuntana.musicrecords.security;

import com.coditramuntana.musicrecords.model.data.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and validates the access tokens of the API.
 *
 * <p>Tokens are signed with HS256 using a symmetric key. A symmetric algorithm is enough here
 * because the same application both issues and verifies the tokens; an asymmetric key would
 * only pay off if a third party had to verify them without being able to mint them.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties properties;

    /**
     * Builds a signed access token carrying the username as subject and the role as a claim.
     */
    public String generateAccessToken(UserAccount user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuer(properties.getIssuer())
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getAccessTokenExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Parses and verifies a token, returning its claims only when signature, issuer and
     * expiration are all valid.
     *
     * <p>Returns an empty optional instead of throwing: an invalid token is an expected
     * condition of a public endpoint, not an exceptional one.
     */
    public Optional<Claims> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            // Covers a tampered signature, an expired token, a wrong issuer and malformed input.
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Username carried by a token that has already been validated.
     */
    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Remaining lifetime of an access token, in seconds, useful for the client.
     */
    public long getAccessTokenExpirationSeconds() {
        return properties.getAccessTokenExpirationMs() / 1000;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }

}

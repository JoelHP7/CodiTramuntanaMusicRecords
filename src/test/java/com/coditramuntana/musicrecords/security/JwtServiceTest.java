package com.coditramuntana.musicrecords.security;

import com.coditramuntana.musicrecords.model.data.Role;
import com.coditramuntana.musicrecords.model.data.UserAccount;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of the token mechanics, without Spring context.
 */
class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zaWduaW5nLWtleS1mb3ItbXVzaWMtcmVjb3Jkcy0yNTZiIQ==";
    private static final String OTHER_SECRET = "YW5vdGhlci1zaWduaW5nLWtleS1mb3ItdGhlc2UtdGVzdHMtMjU2IQ==";

    private JwtService jwtService;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(properties(SECRET, 900_000L));
        user = UserAccount.builder()
                .id(1L)
                .username("admin")
                .password("irrelevant")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should issue a token carrying the username and the role")
    void generateAccessToken_shouldCarryUsernameAndRole() {
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.parseToken(token).orElseThrow();

        assertThat(jwtService.extractUsername(claims)).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuer()).isEqualTo("music-records");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Should reject a token signed with a different key")
    void parseToken_shouldRejectTokenSignedWithAnotherKey() {
        String foreignToken = new JwtService(properties(OTHER_SECRET, 900_000L)).generateAccessToken(user);

        assertThat(jwtService.parseToken(foreignToken)).isEmpty();
    }

    @Test
    @DisplayName("Should reject a token whose payload has been tampered with")
    void parseToken_shouldRejectTamperedToken() {
        String token = jwtService.generateAccessToken(user);
        String[] parts = token.split("\\.");
        // Same header and signature, but a payload that no longer matches them.
        String tampered = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "XY." + parts[2];

        assertThat(jwtService.parseToken(tampered)).isEmpty();
    }

    @Test
    @DisplayName("Should reject an already expired token")
    void parseToken_shouldRejectExpiredToken() {
        JwtService expiredIssuer = new JwtService(properties(SECRET, -1_000L));

        String expiredToken = expiredIssuer.generateAccessToken(user);

        assertThat(jwtService.parseToken(expiredToken)).isEmpty();
    }

    @Test
    @DisplayName("Should reject malformed input instead of throwing")
    void parseToken_shouldReturnEmptyForGarbage() {
        assertThat(jwtService.parseToken("not-a-token")).isEmpty();
        assertThat(jwtService.parseToken("")).isEmpty();
    }

    @Test
    @DisplayName("Should expose the access token lifetime in seconds")
    void getAccessTokenExpirationSeconds_shouldConvertFromMilliseconds() {
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Should reject a token issued by a different issuer")
    void parseToken_shouldRejectForeignIssuer() {
        JwtProperties foreign = properties(SECRET, 900_000L);
        foreign.setIssuer("someone-else");

        String token = new JwtService(foreign).generateAccessToken(user);
        Optional<Claims> claims = jwtService.parseToken(token);

        assertThat(claims).isEmpty();
    }

    private JwtProperties properties(String secret, long accessTokenExpirationMs) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer("music-records");
        properties.setAccessTokenExpirationMs(accessTokenExpirationMs);
        properties.setRefreshTokenExpirationMs(604_800_000L);
        return properties;
    }

}

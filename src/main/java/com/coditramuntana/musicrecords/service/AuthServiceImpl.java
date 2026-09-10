package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.exception.InvalidCredentialsException;
import com.coditramuntana.musicrecords.exception.InvalidRefreshTokenException;
import com.coditramuntana.musicrecords.model.data.RefreshToken;
import com.coditramuntana.musicrecords.model.data.UserAccount;
import com.coditramuntana.musicrecords.model.dto.AuthResponse;
import com.coditramuntana.musicrecords.model.dto.LoginRequest;
import com.coditramuntana.musicrecords.model.dto.UserDto;
import com.coditramuntana.musicrecords.repository.RefreshTokenRepository;
import com.coditramuntana.musicrecords.repository.UserAccountRepository;
import com.coditramuntana.musicrecords.security.JwtProperties;
import com.coditramuntana.musicrecords.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implements the authentication flows: login, refresh, logout and profile.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int REFRESH_TOKEN_BYTES = 48;

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user '{}'", request.getUsername());

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        // The password is checked even for disabled users so that both paths cost the same.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()) || !user.isEnabled()) {
            log.warn("Failed login for user '{}'", request.getUsername());
            throw new InvalidCredentialsException();
        }

        log.info("User '{}' logged in", user.getUsername());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenWithUser(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!stored.isUsable(System.currentTimeMillis())) {
            log.warn("Refresh rejected for user '{}': token revoked or expired",
                    stored.getUser().getUsername());
            throw new InvalidRefreshTokenException();
        }

        UserAccount user = stored.getUser();
        if (!user.isEnabled()) {
            log.warn("Refresh rejected: user '{}' is disabled", user.getUsername());
            throw new InvalidRefreshTokenException();
        }

        // Rotation: the token just used is revoked and a brand new one is issued, so a stolen
        // refresh token is only good until its legitimate owner refreshes once.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        log.info("Refreshed tokens for user '{}'", user.getUsername());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenWithUser(refreshToken).ifPresentOrElse(stored -> {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            log.info("User '{}' logged out", stored.getUser().getUsername());
        }, () -> log.info("Logout requested with an unknown refresh token, nothing to revoke"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(InvalidCredentialsException::new);

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    private AuthResponse issueTokens(UserAccount user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .token(generateRefreshTokenValue())
                .user(user)
                .expiresAt(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpirationMs())
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Refresh tokens are opaque random values, not JWTs: they are only ever looked up in the
     * database, so there is nothing to gain from making them self describing.
     */
    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}

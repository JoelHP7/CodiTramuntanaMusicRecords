package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.model.dto.AuthResponse;
import com.coditramuntana.musicrecords.model.dto.LoginRequest;
import com.coditramuntana.musicrecords.model.dto.UserDto;

public interface AuthService {

    /**
     * Validates credentials and issues a new pair of tokens.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Exchanges a valid refresh token for a new pair, revoking the used one.
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Revokes a refresh token so it can no longer be exchanged.
     */
    void logout(String refreshToken);

    /**
     * Returns the profile of the currently authenticated user.
     */
    UserDto getCurrentUser(String username);

}

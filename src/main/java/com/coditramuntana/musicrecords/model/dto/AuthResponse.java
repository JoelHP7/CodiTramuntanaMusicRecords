package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tokens handed to a client after a successful login or refresh.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Issued tokens")
public class AuthResponse {

    @Schema(description = "Short lived token to be sent in the Authorization header")
    private String accessToken;

    @Schema(description = "Long lived token used to obtain a new access token")
    private String refreshToken;

    @Schema(description = "Type of the access token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Lifetime of the access token in seconds", example = "900")
    private long expiresIn;

    @Schema(description = "Name of the authenticated user", example = "admin")
    private String username;

    @Schema(description = "Role of the authenticated user", example = "ADMIN")
    private String role;

}

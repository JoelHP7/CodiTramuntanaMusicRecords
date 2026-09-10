package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload carrying a refresh token, used to renew or to revoke a session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A previously issued refresh token")
public class RefreshRequest {

    @NotBlank(message = "must not be blank")
    @Schema(description = "Refresh token obtained when logging in")
    private String refreshToken;

}

package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Credentials sent to obtain a pair of tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credentials of a user")
public class LoginRequest {

    @NotBlank(message = "must not be blank")
    @Schema(description = "User name", example = "admin")
    private String username;

    @NotBlank(message = "must not be blank")
    @Schema(description = "Password", example = "admin123")
    private String password;

}

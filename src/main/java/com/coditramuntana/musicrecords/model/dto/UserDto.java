package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public view of the authenticated user. Never carries the password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authenticated user")
public class UserDto {

    @Schema(description = "User identifier", example = "1")
    private Long id;

    @Schema(description = "User name", example = "admin")
    private String username;

    @Schema(description = "Granted role", example = "ADMIN")
    private String role;

}

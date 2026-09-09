package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload to create or update an LP.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to create or update an LP")
public class LpRequest {

    @NotBlank(message = "must not be blank")
    @Size(max = 150, message = "must be at most 150 characters")
    @Schema(description = "LP name", example = "Black Album")
    private String name;

    @Size(max = 5000, message = "must be at most 5000 characters")
    @Schema(description = "Free text description")
    private String description;

    @NotNull(message = "must not be null")
    @Schema(description = "Identifier of the owning artist", example = "1")
    private Long artistId;

}

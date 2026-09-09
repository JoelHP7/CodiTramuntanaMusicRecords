package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload to create or update an artist.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to create or update an artist")
public class ArtistRequest {

    @NotBlank(message = "must not be blank")
    @Size(max = 120, message = "must be at most 120 characters")
    @Schema(description = "Artist name", example = "Metallica")
    private String name;

    @Size(max = 5000, message = "must be at most 5000 characters")
    @Schema(description = "Free text description", example = "American heavy metal band formed in 1981")
    private String description;

}

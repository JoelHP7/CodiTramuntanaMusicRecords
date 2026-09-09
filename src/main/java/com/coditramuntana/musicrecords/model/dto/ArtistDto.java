package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Artist as returned in list responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Artist summary")
public class ArtistDto {

    @Schema(description = "Artist identifier", example = "1")
    private Long id;

    @Schema(description = "Artist name", example = "Metallica")
    private String name;

    @Schema(description = "Free text description")
    private String description;

}

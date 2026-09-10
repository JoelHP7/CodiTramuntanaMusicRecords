package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Artist detail, including the total number of LPs required by the artist show page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Artist detail with its total number of LPs")
public class ArtistDetailDto {

    @Schema(description = "Artist identifier", example = "1")
    private Long id;

    @Schema(description = "Artist name", example = "Metallica")
    private String name;

    @Schema(description = "Free text description")
    private String description;

    @Schema(description = "Total number of LPs owned by this artist", example = "2")
    private long lpCount;

}

package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LP as returned in list responses, including the artist it belongs to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LP summary")
public class LpDto {

    @Schema(description = "LP identifier", example = "1")
    private Long id;

    @Schema(description = "LP name", example = "Black Album")
    private String name;

    @Schema(description = "Free text description")
    private String description;

    @Schema(description = "Identifier of the owning artist", example = "1")
    private Long artistId;

    @Schema(description = "Name of the owning artist", example = "Metallica")
    private String artistName;

}

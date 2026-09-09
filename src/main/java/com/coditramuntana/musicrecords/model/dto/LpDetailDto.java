package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LP detail including its songs and the authors of each song.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LP detail with its songs")
public class LpDetailDto {

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

    @Schema(description = "Songs contained in this LP")
    private List<SongDto> songs;

}

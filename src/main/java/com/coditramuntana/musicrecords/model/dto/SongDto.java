package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Song with the names of its authors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Song with its authors")
public class SongDto {

    @Schema(description = "Song identifier", example = "1")
    private Long id;

    @Schema(description = "Song name", example = "Enter Sandman")
    private String name;

    @Schema(description = "Names of the authors of the song", example = "[\"Hetfield\"]")
    private List<String> authors;

}

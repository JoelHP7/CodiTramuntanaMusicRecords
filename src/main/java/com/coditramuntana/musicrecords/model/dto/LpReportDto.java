package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One row of the discography report shown on the home page.
 *
 * <p>Authors are exposed as a list rather than a pre-joined string: the API returns data
 * and the presentation layer decides how to render it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Row of the discography report")
public class LpReportDto {

    @Schema(description = "LP identifier", example = "1")
    private Long lpId;

    @Schema(description = "LP name", example = "Black Album")
    private String lpName;

    @Schema(description = "Name of the artist owning the LP", example = "Metallica")
    private String artistName;

    @Schema(description = "Number of songs contained in the LP", example = "3")
    private long songCount;

    @Schema(description = "Distinct authors of the songs of the LP",
            example = "[\"Hammett\", \"Hetfield\", \"Jason N.\"]")
    private List<String> authors;

}

package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author of the shared catalogue, used to reuse existing authors from the UI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Song author")
public class AuthorDto {

    @Schema(description = "Author identifier", example = "1")
    private Long id;

    @Schema(description = "Author name", example = "Hetfield")
    private String name;

}

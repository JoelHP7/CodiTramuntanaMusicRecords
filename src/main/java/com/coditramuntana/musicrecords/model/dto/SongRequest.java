package com.coditramuntana.musicrecords.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload to add or update a song inside an LP.
 *
 * <p>Authors are provided by name: existing ones are reused, missing ones are created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to add or update a song")
public class SongRequest {

    @NotBlank(message = "must not be blank")
    @Size(max = 150, message = "must be at most 150 characters")
    @Schema(description = "Song name", example = "Enter Sandman")
    private String name;

    @NotEmpty(message = "must contain at least one author")
    @Schema(description = "Names of the authors of the song", example = "[\"Hetfield\", \"Hammett\"]")
    private List<@NotBlank(message = "must not be blank") String> authorNames;

}

package com.coditramuntana.musicrecords.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Error payload returned by every failing endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error details returned when a request cannot be fulfilled")
public class ErrorResponse {

    @Schema(description = "Moment the error was produced", example = "2026-09-09T20:15:30.123")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP reason phrase", example = "Not Found")
    private String error;

    @Schema(description = "Human readable description of the problem")
    private String message;

}

package com.coditramuntana.musicrecords.controller;

import com.coditramuntana.musicrecords.model.dto.LpReportDto;
import com.coditramuntana.musicrecords.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.coditramuntana.musicrecords.controller.ReportPaths.BASE_PATH;
import static com.coditramuntana.musicrecords.controller.ReportPaths.DISCOGRAPHY_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Aggregated views of the discography")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Discography report",
            description = "Returns one row per LP with its artist, its number of songs and the distinct "
                    + "authors of those songs. Resolved with two aggregated queries, never one per LP."
    )
    @ApiResponse(responseCode = "200", description = "Report rows")
    @GetMapping(value = DISCOGRAPHY_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LpReportDto>> getDiscographyReport() {
        log.info("GET {}{}", BASE_PATH, DISCOGRAPHY_PATH);
        List<LpReportDto> report = reportService.getDiscographyReport();
        log.info("GET {}{} - returning {} rows", BASE_PATH, DISCOGRAPHY_PATH, report.size());
        return ResponseEntity.ok(report);
    }

}

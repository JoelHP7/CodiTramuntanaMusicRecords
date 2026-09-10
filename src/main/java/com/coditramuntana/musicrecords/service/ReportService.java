package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.model.dto.LpReportDto;

import java.util.List;

public interface ReportService {

    /**
     * Builds the discography report shown on the home page.
     */
    List<LpReportDto> getDiscographyReport();

}

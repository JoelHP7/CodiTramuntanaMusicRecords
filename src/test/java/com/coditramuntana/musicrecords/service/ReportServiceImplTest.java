package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.mapper.ReportAssembler;
import com.coditramuntana.musicrecords.model.dto.LpReportDto;
import com.coditramuntana.musicrecords.model.projection.LpAuthorRow;
import com.coditramuntana.musicrecords.model.projection.LpReportRow;
import com.coditramuntana.musicrecords.repository.LpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The assembling of the report is a pure function over the two aggregated queries, so it
 * is verified here without touching the database.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private LpRepository lpRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(lpRepository, new ReportAssembler());
    }

    @Test
    @DisplayName("Should attach to every LP only the authors of its own songs")
    void getReport_shouldGroupAuthorsByLp() {
        when(lpRepository.findReportRows()).thenReturn(List.of(
                new LpReportRow(1L, "Black album", "Metallica", 3L),
                new LpReportRow(3L, "Against", "Sepultura", 1L)));
        when(lpRepository.findReportAuthorRows()).thenReturn(List.of(
                new LpAuthorRow(1L, "Hammett"),
                new LpAuthorRow(1L, "Hetfield"),
                new LpAuthorRow(1L, "Jason N."),
                new LpAuthorRow(3L, "Jason N.")));

        List<LpReportDto> report = reportService.getDiscographyReport();

        assertThat(report).hasSize(2);
        assertThat(report.get(0).getLpName()).isEqualTo("Black album");
        assertThat(report.get(0).getSongCount()).isEqualTo(3L);
        assertThat(report.get(0).getAuthors()).containsExactly("Hammett", "Hetfield", "Jason N.");
        assertThat(report.get(1).getAuthors()).containsExactly("Jason N.");
    }

    @Test
    @DisplayName("Should keep an LP without songs in the report with zero songs and no authors")
    void getReport_shouldReturnEmptyAuthorListForLpWithoutSongs() {
        when(lpRepository.findReportRows()).thenReturn(List.of(
                new LpReportRow(6L, "Piece of Mind", "Iron Maiden", 0L)));
        when(lpRepository.findReportAuthorRows()).thenReturn(List.of());

        List<LpReportDto> report = reportService.getDiscographyReport();

        assertThat(report).hasSize(1);
        assertThat(report.get(0).getSongCount()).isZero();
        assertThat(report.get(0).getAuthors()).isEmpty();
    }

    @Test
    @DisplayName("Should preserve the author order already applied by the query")
    void getReport_shouldKeepAuthorOrderFromQuery() {
        when(lpRepository.findReportRows()).thenReturn(List.of(
                new LpReportRow(4L, "The Dark Side of the Moon", "Pink Floyd", 2L)));
        when(lpRepository.findReportAuthorRows()).thenReturn(List.of(
                new LpAuthorRow(4L, "Gilmour"),
                new LpAuthorRow(4L, "Mason"),
                new LpAuthorRow(4L, "Waters"),
                new LpAuthorRow(4L, "Wright")));

        List<LpReportDto> report = reportService.getDiscographyReport();

        assertThat(report.get(0).getAuthors())
                .containsExactly("Gilmour", "Mason", "Waters", "Wright");
    }

    @Test
    @DisplayName("Should never drop an LP just because it has no authors")
    void getReport_shouldNotDropLpsThatHaveNoAuthors() {
        when(lpRepository.findReportRows()).thenReturn(List.of(
                new LpReportRow(1L, "With authors", "Artist", 1L),
                new LpReportRow(2L, "Without authors", "Artist", 0L)));
        when(lpRepository.findReportAuthorRows()).thenReturn(List.of(
                new LpAuthorRow(1L, "Someone")));

        List<LpReportDto> report = reportService.getDiscographyReport();

        assertThat(report).extracting(LpReportDto::getLpName)
                .containsExactly("With authors", "Without authors");
    }

    @Test
    @DisplayName("Should report zero songs when the aggregated count comes back null")
    void getReport_shouldTreatNullCountAsZero() {
        when(lpRepository.findReportRows()).thenReturn(List.of(
                new LpReportRow(1L, "Odd row", "Artist", null)));
        when(lpRepository.findReportAuthorRows()).thenReturn(List.of());

        List<LpReportDto> report = reportService.getDiscographyReport();

        assertThat(report.get(0).getSongCount()).isZero();
    }

}

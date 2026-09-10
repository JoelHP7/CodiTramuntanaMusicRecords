package com.coditramuntana.musicrecords.service;

import com.coditramuntana.musicrecords.mapper.ReportAssembler;
import com.coditramuntana.musicrecords.model.dto.LpReportDto;
import com.coditramuntana.musicrecords.model.projection.LpAuthorRow;
import com.coditramuntana.musicrecords.model.projection.LpReportRow;
import com.coditramuntana.musicrecords.repository.LpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the discography report of the home page.
 *
 * <p>The report is resolved with exactly two aggregated queries, regardless of how many
 * LPs, songs or authors exist, so it cannot degrade into an N+1 problem.
 *
 * <p>A single native query using {@code GROUP_CONCAT} was evaluated and discarded:
 * SQLite cannot combine {@code DISTINCT} with a custom separator, does not guarantee the
 * ordering of the concatenated values, and the resulting SQL would be engine specific.
 * Fetch joining the whole graph was discarded too, since it produces a cartesian product
 * just to compute a count.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LpRepository lpRepository;
    private final ReportAssembler reportAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<LpReportDto> getDiscographyReport() {
        List<LpReportRow> rows = lpRepository.findReportRows();
        Map<Long, List<String>> authorsByLp = groupAuthorsByLp(lpRepository.findReportAuthorRows());

        log.info("Discography report built for {} LP(s)", rows.size());

        return rows.stream()
                .map(row -> reportAssembler.toDto(row, authorsByLp.getOrDefault(row.getLpId(), List.of())))
                .toList();
    }

    /**
     * Groups the (LP, author) pairs by LP, preserving the alphabetical order the query
     * already applied so the rendered report is deterministic.
     */
    private Map<Long, List<String>> groupAuthorsByLp(List<LpAuthorRow> authorRows) {
        return authorRows.stream()
                .collect(Collectors.groupingBy(
                        LpAuthorRow::getLpId,
                        LinkedHashMap::new,
                        Collectors.mapping(LpAuthorRow::getAuthorName, Collectors.toList())));
    }

}

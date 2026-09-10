package com.coditramuntana.musicrecords.mapper;

import com.coditramuntana.musicrecords.model.dto.LpReportDto;
import com.coditramuntana.musicrecords.model.projection.LpReportRow;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportAssembler {

    public LpReportDto toDto(LpReportRow row, List<String> authors) {
        return LpReportDto.builder()
                .lpId(row.getLpId())
                .lpName(row.getLpName())
                .artistName(row.getArtistName())
                .songCount(row.getSongCount() == null ? 0L : row.getSongCount())
                .authors(authors)
                .build();
    }

}

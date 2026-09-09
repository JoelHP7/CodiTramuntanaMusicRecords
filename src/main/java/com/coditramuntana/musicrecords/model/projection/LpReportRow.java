package com.coditramuntana.musicrecords.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated row of the discography report: one entry per LP with its song count.
 *
 * <p>Consumed through a JPQL constructor expression, hence the all-args constructor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LpReportRow {

    private Long lpId;
    private String lpName;
    private String artistName;
    private Long songCount;

}

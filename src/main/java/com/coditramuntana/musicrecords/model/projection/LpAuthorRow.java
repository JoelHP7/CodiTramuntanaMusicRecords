package com.coditramuntana.musicrecords.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Distinct (LP, author) pair used to build the "Authors" column of the report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LpAuthorRow {

    private Long lpId;
    private String authorName;

}

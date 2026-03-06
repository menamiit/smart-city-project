package com.menamiit.smartcityproject.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class GrievanceStatsResponse {
    private long total;
    private long pending;
    private long inProgress;
    private long resolved;
    private long high;
    private long critical;
}

package com.menamiit.smartcityproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedZoneDto {
    private String zone;
    private Long totalComplaints;
    private Long reopenedComplaints;
    private Double reopenRatePercent;
}
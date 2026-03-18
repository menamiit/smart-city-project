package com.menamiit.smartcityproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlaSummaryDto {
    private Long totalResolved;
    private Long onTimeResolved;
    private Long lateResolved;
    private Double compliancePercent;
    private Double avgResolutionHours;
}
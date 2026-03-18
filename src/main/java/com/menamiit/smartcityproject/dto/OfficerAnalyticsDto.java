package com.menamiit.smartcityproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficerAnalyticsDto {
    private String officer;
    private Long assigned;
    private Long pending;
    private Long inProgress;
    private Long completed;
    private SlaSummaryDto sla;
}
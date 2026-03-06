package com.menamiit.smartcityproject.dto;

import lombok.Data;

@Data
public class AdminUpdateRequest {
    private String status;
    private String remarks;
    private String assignedOfficer;
    private String deadline;
    private String priority;
}

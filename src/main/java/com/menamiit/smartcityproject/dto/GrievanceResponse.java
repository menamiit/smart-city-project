package com.menamiit.smartcityproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class GrievanceResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String location;
    private String imageBase64;
    private String citizenUsername;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String assignedOfficer;
    private String remarks;
}
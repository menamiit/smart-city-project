package com.menamiit.smartcityproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String adminNotes;
    private String priority;
    private String department;
    private Integer rating;
    private String feedbackComment;
    private LocalDateTime feedbackSubmittedAt;
    private String reopenReason;
    private LocalDateTime reopenedAt;
    private Integer reopenCount;
}
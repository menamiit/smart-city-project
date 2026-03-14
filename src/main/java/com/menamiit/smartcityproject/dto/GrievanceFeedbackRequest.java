package com.menamiit.smartcityproject.dto;

import lombok.Data;

@Data
public class GrievanceFeedbackRequest {
    private Integer rating;
    private String comment;
}

package com.menamiit.smartcityproject.dto;
 
import lombok.Data;
 
@Data
public class GrievanceRequest {
    private String title;
    private String description;
    private String category;
    private String location;
    private String imageBase64;
}
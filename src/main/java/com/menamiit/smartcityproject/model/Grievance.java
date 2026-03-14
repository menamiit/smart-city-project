package com.menamiit.smartcityproject.model;
 
import java.time.LocalDateTime;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity
@Table(name = "grievances")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class Grievance {
    
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String title;
 
    @Column(nullable = false, length = 1000)
    private String description;
 
    @Column(nullable = false)
    private String category; // WATER, STREET_LIGHT, ROAD, SANITATION, DRAINAGE, PARK, ELECTRICITY, OTHER
 
    @Column(nullable = false)
    private String status;   // PENDING, IN_PROGRESS, RESOLVED, CLOSED
 
    private String location;

    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
 
    @Column(length = 5000000) // ~5MB base64 image
    private String imageBase64;
 
    @Column(nullable = false)
    private String citizenUsername;
 
    @Column(nullable = false)
    private LocalDateTime submittedAt;
 
    private LocalDateTime updatedAt;

    private LocalDateTime deadline;
 
    private String assignedOfficer;
 
    private String remarks;

    private String department;   // Roads, Water Supply, Electricity, etc.

    @Column(columnDefinition = "TEXT")
    private String adminNotes;   // admin notes shown in view modal

    private Integer rating; // 1..5 citizen feedback score

    @Column(columnDefinition = "TEXT")
    private String feedbackComment;

    private LocalDateTime feedbackSubmittedAt;

    @Column(columnDefinition = "TEXT")
    private String reopenReason;

    private LocalDateTime reopenedAt;

    private Integer reopenCount;

    @PrePersist
    protected void onCreate() {
        if(submittedAt==null) {
            submittedAt = LocalDateTime.now();
        }

        if(status == null) {
            status = "PENDING";
        }

        if(priority == null) {
            priority = "MEDIUM";
        }

        if (reopenCount == null) {
            reopenCount = 0;
        }
    }
}

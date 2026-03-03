package com.menamiit.smartcityproject.dto;

import java.time.LocalDateTime;

// Request payload for admin assignment of a grievance to an officer
public class AdminAssignRequest {
	private Long grievanceId;
	private String assignedOfficer;
	private String status;
	private String remarks;
	private LocalDateTime deadline;

	public Long getGrievanceId() { return grievanceId; }
	public void setGrievanceId(Long grievanceId) { this.grievanceId = grievanceId; }

	public String getAssignedOfficer() { return assignedOfficer; }
	public void setAssignedOfficer(String assignedOfficer) { this.assignedOfficer = assignedOfficer; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = remarks; }

	public LocalDateTime getDeadline() { return deadline; }
	public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}

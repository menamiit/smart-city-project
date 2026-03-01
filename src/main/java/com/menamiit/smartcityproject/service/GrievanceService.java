package com.menamiit.smartcityproject.service;

import com.menamiit.smartcityproject.dto.GrievanceRequest;
import com.menamiit.smartcityproject.dto.GrievanceResponse;
import com.menamiit.smartcityproject.model.Grievance;
import com.menamiit.smartcityproject.repository.GrievanceRepository;
import com.menamiit.smartcityproject.security.JwtUtil;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
 
@Service
public class GrievanceService {
    
 
    @Autowired
    private GrievanceRepository grievanceRepository;
 
    @Autowired
    private JwtUtil jwtUtil;
 
    private static final List<String> VALID_CATEGORIES = List.of(
        "WATER", "STREET_LIGHT", "ROAD", "SANITATION", "DRAINAGE", "PARK", "ELECTRICITY", "OTHER"
    );
 
    // ── Submit new grievance ──────────────────────────────────────────────────
    public GrievanceResponse submit(GrievanceRequest request, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
 
        String category = request.getCategory() != null
            ? request.getCategory().toUpperCase() : "OTHER";
 
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category.");
        }
 
        Grievance g = new Grievance();
        g.setTitle(request.getTitle());
        g.setDescription(request.getDescription());
        g.setCategory(category);
        g.setStatus("PENDING");
        g.setLocation(request.getLocation());
        g.setImageBase64(request.getImageBase64());
        g.setCitizenUsername(username);
        g.setSubmittedAt(LocalDateTime.now());
 
        grievanceRepository.save(g);
        return toResponse(g);
    }
 
    // ── Get grievances for logged-in citizen ──────────────────────────────────
    public List<GrievanceResponse> getMyGrievances(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return grievanceRepository
            .findByCitizenUsernameOrderBySubmittedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── Get single grievance by ID ─────────────────────────────────────────────
    public GrievanceResponse getById(Long id, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        String role     = jwtUtil.getRoleFromToken(token);
 
        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));
 
        // Citizens can only view their own grievances
        if (role.equals("CITIZEN") && !g.getCitizenUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied.");
        }
 
        return toResponse(g);
    }
 
    // ── Get all grievances (Admin/Officer) ────────────────────────────────────
    public List<GrievanceResponse> getAll() {
        return grievanceRepository.findAllByOrderBySubmittedAtDesc()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── Map entity to response ────────────────────────────────────────────────
    private GrievanceResponse toResponse(Grievance g) {
        return new GrievanceResponse(
            g.getId(), g.getTitle(), g.getDescription(),
            g.getCategory(), g.getStatus(), g.getLocation(),
            g.getImageBase64(), g.getCitizenUsername(),
            g.getSubmittedAt(), g.getUpdatedAt(),
            g.getAssignedOfficer(), g.getRemarks()
        );
    }
}
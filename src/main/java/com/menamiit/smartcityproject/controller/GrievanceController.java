package com.menamiit.smartcityproject.controller;
 
import com.menamiit.smartcityproject.dto.GrievanceRequest;
import com.menamiit.smartcityproject.dto.GrievanceResponse;
import com.menamiit.smartcityproject.service.GrievanceService;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/grievances")
@CrossOrigin(origins = "*")

public class GrievanceController {
    
 
    @Autowired
    private GrievanceService grievanceService;
 
    // Extract token from Authorization header
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        }
        return authHeader.substring(7);
    }
 
    // ── POST /api/grievances/submit ───────────────────────────────────────────
    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestBody GrievanceRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            GrievanceResponse response = grievanceService.submit(request, token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/my ────────────────────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<?> getMyGrievances(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<GrievanceResponse> list = grievanceService.getMyGrievances(token);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            GrievanceResponse response = grievanceService.getById(id, token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/all (Admin/Officer only) ──────────────────────────
    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader) {
        try {
            extractToken(authHeader);
            List<GrievanceResponse> list = grievanceService.getAll();
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

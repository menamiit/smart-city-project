package com.menamiit.smartcityproject.controller;
  
import com.menamiit.smartcityproject.dto.AdminAssignRequest;
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
 
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }
 
    // ── POST /api/grievances/submit ───────────────────────────────────────────
    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestBody GrievanceRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.submit(request, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/my ────────────────────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<?> getMyGrievances(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<GrievanceResponse> list = grievanceService.getMyGrievances(extractToken(authHeader));
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/all (Admin/Officer) ───────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader) {
        try {
            extractToken(authHeader);
            List<GrievanceResponse> list = grievanceService.getAll();
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/filter?status=PENDING ─────────────────────────────
    @GetMapping("/filter")
    public ResponseEntity<?> filterByStatus(
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        try {
            extractToken(authHeader);
            return ResponseEntity.ok(grievanceService.getByStatus(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── GET /api/grievances/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getById(id, extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── POST /api/grievances/admin/assign  (ADMIN only) ───────────────────────
    @PostMapping("/admin/assign")
    public ResponseEntity<?> adminAssign(
            @RequestBody AdminAssignRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.adminAssign(request, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // ── PUT /api/grievances/{id}/status  (ADMIN/OFFICER) ─────────────────────
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String status  = body.get("status");
            String remarks = body.get("remarks");
            GrievanceResponse response = grievanceService.updateStatus(id, status, remarks, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
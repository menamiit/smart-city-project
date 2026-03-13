package com.menamiit.smartcityproject.controller;

import com.menamiit.smartcityproject.service.GrievanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/officer")
@CrossOrigin(origins = "*")
public class OfficerController {

    @Autowired
    private GrievanceService grievanceService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        }
        return authHeader.substring(7);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getAssignedTasks(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerTasks(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tasks/in-progress")
    public ResponseEntity<?> getInProgressTasks(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerTasksByStatuses(extractToken(authHeader), Set.of("IN_PROGRESS")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tasks/completed")
    public ResponseEntity<?> getCompletedTasks(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerTasksByStatuses(extractToken(authHeader), Set.of("RESOLVED", "CLOSED")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getOfficerStats(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerStats(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getOfficerProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerProfile(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.updateOfficerTaskStatus(
                id,
                body.get("status"),
                body.get("remarks"),
                extractToken(authHeader)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
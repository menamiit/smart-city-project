package com.menamiit.smartcityproject.controller;

import com.menamiit.smartcityproject.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        }
        return authHeader.substring(7);
    }

    @GetMapping("/distribution/category")
    public ResponseEntity<?> categoryDistribution(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(analyticsService.getCategoryDistribution(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/distribution/zone")
    public ResponseEntity<?> zoneDistribution(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(analyticsService.getZoneDistribution(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sla/summary")
    public ResponseEntity<?> slaSummary(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(analyticsService.getSlaSummary(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/red-zones")
    public ResponseEntity<?> redZones(
        @RequestParam(defaultValue = "3") int minComplaints,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            return ResponseEntity.ok(analyticsService.getRedZones(extractToken(authHeader), minComplaints));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/officer/me")
    public ResponseEntity<?> officerMine(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(analyticsService.getOfficerMyAnalytics(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

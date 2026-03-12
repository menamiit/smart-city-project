package com.menamiit.smartcityproject.controller;

import com.menamiit.smartcityproject.dto.UserResponse;
import com.menamiit.smartcityproject.model.User;
import com.menamiit.smartcityproject.repository.UserRepository;
import com.menamiit.smartcityproject.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }

    private void requireAdmin(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role))
            throw new IllegalArgumentException("Access denied. Admins only.");
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.getEmail(), u.getPhone());
    }

    // ── GET /api/admin/users ─────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            requireAdmin(token);
            List<UserResponse> users = userRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/admin/officers ──────────────────────────────────────────────
    @GetMapping("/officers")
    public ResponseEntity<?> getOfficers(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            requireAdmin(token);
            List<UserResponse> officers = userRepository.findByRole("OFFICER")
                .stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(officers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // ── PUT /api/admin/users/{id}/role ───────────────────────────────────────
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            requireAdmin(token);

            String newRole = body.get("role");
            if (newRole == null || !List.of("CITIZEN", "ADMIN", "OFFICER").contains(newRole.toUpperCase())) {
                throw new IllegalArgumentException("Invalid role. Must be CITIZEN, ADMIN, or OFFICER.");
            }

            User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

            user.setRole(newRole.toUpperCase());
            userRepository.save(user);
            return ResponseEntity.ok(toResponse(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE /api/admin/users/{id} ─────────────────────────────────────────
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            requireAdmin(token);

            String adminUsername = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

            if (user.getUsername().equals(adminUsername)) {
                throw new IllegalArgumentException("Cannot delete your own account.");
            }

            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

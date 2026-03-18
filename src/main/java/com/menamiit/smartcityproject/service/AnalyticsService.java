package com.menamiit.smartcityproject.service;

import com.menamiit.smartcityproject.dto.LabelCountDto;
import com.menamiit.smartcityproject.dto.OfficerAnalyticsDto;
import com.menamiit.smartcityproject.dto.RedZoneDto;
import com.menamiit.smartcityproject.dto.SlaSummaryDto;
import com.menamiit.smartcityproject.model.Grievance;
import com.menamiit.smartcityproject.repository.GrievanceRepository;
import com.menamiit.smartcityproject.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${analytics.sla.default-hours:72}")
    private long defaultSlaHours;

    @Value("${analytics.sla.matrix:}")
    private String slaMatrixRaw;

    private final Map<String, Long> slaMatrix = new HashMap<>();

    @PostConstruct
    void initSlaMatrix() {
        slaMatrix.clear();
        if (slaMatrixRaw == null || slaMatrixRaw.isBlank()) return;

        String[] entries = slaMatrixRaw.split(",");
        for (String entry : entries) {
            String cleaned = entry.trim();
            if (cleaned.isEmpty() || !cleaned.contains("=")) continue;

            String[] pair = cleaned.split("=", 2);
            String key = pair[0].trim().toUpperCase(Locale.ROOT);
            String value = pair[1].trim();

            try {
                slaMatrix.put(key, Long.parseLong(value));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public List<LabelCountDto> getCategoryDistribution(String token) {
        requireAdmin(token);
        List<Object[]> raw = grievanceRepository.countByCategory();
        List<LabelCountDto> out = new ArrayList<>();
        for (Object[] row : raw) {
            String label = row[0] == null ? "OTHER" : row[0].toString();
            out.add(new LabelCountDto(label, toLong(row[1])));
        }
        return out;
    }

    public List<LabelCountDto> getZoneDistribution(String token) {
        requireAdmin(token);
        List<Object[]> raw = grievanceRepository.countByZone();
        List<LabelCountDto> out = new ArrayList<>();
        for (Object[] row : raw) {
            String label = row[0] == null ? "UNKNOWN" : row[0].toString();
            out.add(new LabelCountDto(label, toLong(row[1])));
        }
        return out;
    }

    public SlaSummaryDto getSlaSummary(String token) {
        requireAdmin(token);
        return buildSlaSummary(grievanceRepository.findResolvedForSla());
    }

    public List<RedZoneDto> getRedZones(String token, int minComplaints) {
        requireAdmin(token);
        int threshold = Math.max(1, minComplaints);
        List<Object[]> raw = grievanceRepository.findRedZones(threshold);
        List<RedZoneDto> out = new ArrayList<>();

        for (Object[] row : raw) {
            String zone = row[0] == null ? "UNKNOWN" : row[0].toString();
            long total = toLong(row[1]);
            long reopened = toLong(row[2]);
            double rate = total == 0 ? 0.0 : round2((reopened * 100.0) / total);
            out.add(new RedZoneDto(zone, total, reopened, rate));
        }
        return out;
    }

    public OfficerAnalyticsDto getOfficerMyAnalytics(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"OFFICER".equals(role)) {
            throw new IllegalArgumentException("Access denied.");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        List<Grievance> tasks = grievanceRepository.findByAssignedOfficerOrderBySubmittedAtDesc(username);

        long assigned = tasks.size();
        long pending = tasks.stream().filter(g -> "PENDING".equals(g.getStatus())).count();
        long inProgress = tasks.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count();
        long completed = tasks.stream().filter(g -> "RESOLVED".equals(g.getStatus()) || "CLOSED".equals(g.getStatus())).count();

        List<Grievance> resolved = tasks.stream()
            .filter(g -> ("RESOLVED".equals(g.getStatus()) || "CLOSED".equals(g.getStatus()))
                && g.getSubmittedAt() != null
                && g.getUpdatedAt() != null)
            .toList();

        return new OfficerAnalyticsDto(
            username,
            assigned,
            pending,
            inProgress,
            completed,
            buildSlaSummary(resolved)
        );
    }

    private SlaSummaryDto buildSlaSummary(List<Grievance> resolvedList) {
        long total = resolvedList.size();
        if (total == 0) {
            return new SlaSummaryDto(0L, 0L, 0L, 0.0, 0.0);
        }

        long onTime = 0;
        double hoursSum = 0.0;

        for (Grievance g : resolvedList) {
            double hours = resolutionHours(g);
            hoursSum += hours;

            long target = targetHours(g);
            if (hours <= target) onTime++;
        }

        long late = total - onTime;
        double compliance = round2((onTime * 100.0) / total);
        double avg = round2(hoursSum / total);

        return new SlaSummaryDto(total, onTime, late, compliance, avg);
    }

    private long targetHours(Grievance g) {
        String category = valueOrDefault(g.getCategory(), "OTHER");
        String priority = valueOrDefault(g.getPriority(), "MEDIUM");

        String exactKey = category + ":" + priority;
        String categoryWildcard = category + ":*";
        String priorityWildcard = "*:" + priority;

        if (slaMatrix.containsKey(exactKey)) return slaMatrix.get(exactKey);
        if (slaMatrix.containsKey(categoryWildcard)) return slaMatrix.get(categoryWildcard);
        if (slaMatrix.containsKey(priorityWildcard)) return slaMatrix.get(priorityWildcard);

        return defaultSlaHours;
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private double resolutionHours(Grievance g) {
        long minutes = Duration.between(g.getSubmittedAt(), g.getUpdatedAt()).toMinutes();
        return minutes / 60.0;
    }

    private void requireAdmin(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Access denied.");
        }
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package com.portly.controller;

import com.portly.dto.GlobalAnalyticsResponse;
import com.portly.dto.PortfolioAnalyticsResponse;
import com.portly.dto.TrackEventRequest;
import com.portly.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")

@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ─── Tracking endpoints (públicos) ──────────────────────────────────────

    @PostMapping("/track/visit")
    public ResponseEntity<Map<String, Long>> trackVisit(@RequestBody TrackEventRequest request) {
        Long visitId = analyticsService.trackVisit(request);
        return ResponseEntity.ok(Map.of("visitId", visitId));
    }

    @PostMapping("/track/visit-duration")
    public ResponseEntity<Void> trackVisitDuration(@RequestBody TrackEventRequest request) {
        analyticsService.updateVisitDuration(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/track/project-click")
    public ResponseEntity<Void> trackProjectClick(@RequestBody TrackEventRequest request) {
        analyticsService.trackProjectClick(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/track/section-click")
    public ResponseEntity<Void> trackSectionClick(@RequestBody TrackEventRequest request) {
        analyticsService.trackSectionClick(request);
        return ResponseEntity.ok().build();
    }

    // ─── Consulta de analíticas (autenticado) ───────────────────────────────

    @GetMapping("/portfolio/{id}")
    public ResponseEntity<PortfolioAnalyticsResponse> getPortfolioAnalytics(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "all") String period) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(analyticsService.getPortfolioAnalytics(userId, id, period));
    }

    @GetMapping("/global")
    public ResponseEntity<GlobalAnalyticsResponse> getGlobalAnalytics(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all") String period) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(analyticsService.getGlobalAnalytics(userId, period));
    }
}

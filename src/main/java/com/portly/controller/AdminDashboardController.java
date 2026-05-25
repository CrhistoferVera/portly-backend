package com.portly.controller;

import com.portly.dto.DashboardStatsResponse;
import com.portly.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(adminDashboardService.getDashboardStats());
    }
}

package com.swipelab.controller;

import com.swipelab.dto.response.DashboardStatsResponse;
import com.swipelab.service.analytics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StatisticsService statisticsService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(statisticsService.getGlobalStats());
    }
}

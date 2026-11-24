package com.nextround.nextroundapi.controller;


import com.nextround.nextroundapi.dtos.DashboardActivityResponse;
import com.nextround.nextroundapi.dtos.DashboardStatsResponse;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(@AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(dashboardService.getStats(currentUser.getId()));
    }

    @GetMapping("/activity")
    public ResponseEntity<DashboardActivityResponse> getDashboardActivity(@AuthenticationPrincipal User currentUser,
                                                                          @RequestParam(required = false, defaultValue = "WEEK") String granularity){
        return ResponseEntity.ok(dashboardService.getActivity(currentUser.getId(), granularity));
    }
}

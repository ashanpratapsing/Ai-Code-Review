package com.student.demo.controller;

import com.student.demo.dto.DashboardSummaryDTO;
import com.student.demo.security.SecurityUtil;
import com.student.demo.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtil securityUtil;

    public DashboardController(DashboardService dashboardService, SecurityUtil securityUtil) {
        this.dashboardService = dashboardService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/summary")
    public DashboardSummaryDTO getDashboard() {
        return dashboardService.getSummaryForUser(securityUtil.requireCurrentUserId());
    }
}

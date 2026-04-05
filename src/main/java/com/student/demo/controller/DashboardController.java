package com.student.demo.controller;

import com.student.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CodeFileRepository codeFileRepository;

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private AIReportRepository aiReportRepository;

    @GetMapping("/summary")
    public Map<String, Object> getDashboard() {

        Map<String, Object> data = new HashMap<>();

        data.put("totalProjects", projectRepository.count());
        data.put("totalFiles", codeFileRepository.count());
        data.put("totalMetrics", metricsRepository.count());
        data.put("totalAIReports", aiReportRepository.count());

        return data;
    }
}

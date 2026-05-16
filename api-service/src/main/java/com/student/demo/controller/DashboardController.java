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

    @GetMapping("/summary")
    public Map<String, Object> getDashboard() {

        Map<String, Object> data = new HashMap<>();

        data.put("totalProjects", projectRepository.count());
        data.put("totalFilesAnalyzed", codeFileRepository.count());
        data.put("criticalIssues", metricsRepository.count()); // Using metrics as a proxy for now
        data.put("score", 85); // Mock score
        
        // Mock activity data for the chart
        java.util.List<Map<String, Object>> activity = new java.util.ArrayList<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            Map<String, Object> d = new HashMap<>();
            d.put("name", day);
            d.put("issues", (int)(Math.random() * 10));
            d.put("files", (int)(Math.random() * 20));
            activity.add(d);
        }
        data.put("activityData", activity);

        return data;
    }
}

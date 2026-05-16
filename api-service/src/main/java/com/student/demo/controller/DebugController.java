package com.student.demo.controller;

import com.student.demo.entity.Metrics;
import com.student.demo.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private MetricsRepository metricsRepository;

    @GetMapping("/metrics")
    public List<Map<String, Object>> getAllMetrics() {
        return metricsRepository.findAll().stream().map(m -> {
            return Map.of(
                "id", m.getId(),
                "fileId", m.getCodeFile() != null ? m.getCodeFile().getId() : "null",
                "score", m.getComplexityScore(),
                "summary", m.getSummary() != null ? m.getSummary() : "null",
                "bugs", m.getBugs() != null ? m.getBugs() : "null",
                "status", m.getStatus() != null ? m.getStatus().toString() : "null"
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/metrics/{id}")
    public Metrics getFullMetrics(@PathVariable Long id) {
        return metricsRepository.findById(id).orElse(null);
    }
}

package com.student.demo.controller;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.service.CodeAnalyzerService;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyze")
public class CodeAnalyzerController {

    @Autowired
    private CodeAnalyzerService analyzerService;

    @Autowired
    private CodeFileRepository codeFileRepository;

    @Autowired
    private MetricsRepository metricsRepository;

    @PostMapping("/{fileId}")
    public Metrics analyze(@PathVariable Long fileId) {
        CodeFile file = codeFileRepository.findById(fileId).orElseThrow();
        return analyzerService.analyzeCode(file);
    }

    @GetMapping("/{fileId}")
    public Metrics getMetrics(@PathVariable Long fileId) {
        return metricsRepository.findByCodeFileId(fileId).orElseThrow();
    }
}
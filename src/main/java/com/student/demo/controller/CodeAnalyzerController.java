package com.student.demo.controller;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.service.CodeAnalyzerService;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public Map<String, Object> analyze(@PathVariable Long fileId) {
        CodeFile file = codeFileRepository.findById(fileId).orElseThrow();
        Metrics metrics = analyzerService.analyzeCode(file);
        return mapToEnhancedResponse(metrics);
    }

    @PostMapping("/async/{fileId}")
    public ResponseEntity<String> analyzeAsync(@PathVariable Long fileId) {
        CodeFile file = codeFileRepository.findById(fileId).orElseThrow();
        analyzerService.analyzeCodeAsync(file);
        return ResponseEntity.accepted().body("Analysis started in background for file ID: " + fileId);
    }

    @GetMapping("/{fileId}")
    public Map<String, Object> getMetrics(@PathVariable Long fileId) {
        Metrics metrics = metricsRepository.findByCodeFileId(fileId).orElseThrow();
        return mapToEnhancedResponse(metrics);
    }

    private Map<String, Object> mapToEnhancedResponse(Metrics m) {
        Map<String, Object> res = new HashMap<>();
        res.put("score", m.getComplexityScore());
        res.put("summary", m.getSummary());
        res.put("issues", m.getBugs() != null ? Arrays.asList(m.getBugs().split("\n")) : Collections.emptyList());
        res.put("betterApproach", m.getBetterApproach());
        res.put("timeComplexity", m.getTimeComplexity());
        res.put("spaceComplexity", m.getSpaceComplexity());
        res.put("optimizedCode", m.getRefactoredCode());
        res.put("faangInsights", m.getFaangInsights());
        return res;
    }
}

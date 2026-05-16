package com.student.demo.controller;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.service.CodeAnalyzerService;
import com.student.demo.service.SseService;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    
    @Autowired
    private SseService sseService;

    @PostMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> analyze(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "AUTO") String model) {
        CodeFile file = codeFileRepository.findById(fileId).orElseThrow();
        analyzerService.submitAnalysisTask(file, model);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Analysis request accepted. Processing in background.");
        response.put("fileId", fileId.toString());
        response.put("status", "PENDING");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // The /async endpoint was removed as AI processing is now exclusively handled by the RabbitMQ worker.

    @GetMapping("/{fileId}")
    public Map<String, Object> getMetrics(@PathVariable Long fileId) {
        Metrics metrics = metricsRepository.findByCodeFileId(fileId).orElseThrow();
        return mapToEnhancedResponse(metrics);
    }
    
    @GetMapping(path = "/{fileId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics(@PathVariable Long fileId) {
        return sseService.createEmitter(fileId);
    }

    private Map<String, Object> mapToEnhancedResponse(Metrics m) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", m.getStatus());
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

package com.student.demo.controller;

import com.student.demo.dto.AnalysisContextDTO;
import com.student.demo.entity.Metrics;
import com.student.demo.security.SecurityUtil;
import com.student.demo.service.CodeAnalyzerService;
import com.student.demo.service.OwnershipService;
import com.student.demo.service.SseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@RestController
@RequestMapping("/analyze")
public class CodeAnalyzerController {

    private final CodeAnalyzerService analyzerService;
    private final OwnershipService ownershipService;
    private final SseService sseService;
    private final SecurityUtil securityUtil;

    public CodeAnalyzerController(CodeAnalyzerService analyzerService,
                                  OwnershipService ownershipService,
                                  SseService sseService,
                                  SecurityUtil securityUtil) {
        this.analyzerService = analyzerService;
        this.ownershipService = ownershipService;
        this.sseService = sseService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> analyze(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "AUTO") String model,
            @RequestBody(required = false) AnalysisContextDTO executionContext) {
        Long userId = securityUtil.requireCurrentUserId();
        analyzerService.submitAnalysisTask(fileId, userId, model, executionContext);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Analysis request accepted. Processing in background.");
        response.put("fileId", fileId.toString());
        response.put("status", "PENDING");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{fileId}")
    public Map<String, Object> getMetrics(@PathVariable Long fileId) {
        Long userId = securityUtil.requireCurrentUserId();
        Metrics metrics = ownershipService.requireOwnedMetrics(fileId, userId);
        return mapToEnhancedResponse(metrics);
    }

    @GetMapping(path = "/{fileId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics(@PathVariable Long fileId) {
        ownershipService.assertCodeFileOwned(fileId, securityUtil.requireCurrentUserId());
        return sseService.createEmitter(fileId);
    }

    private Map<String, Object> mapToEnhancedResponse(Metrics m) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", m.getStatus());
        res.put("score", m.getComplexityScore());
        res.put("summary", m.getSummary());
        res.put("codeQuality", m.getCodeQuality());
        res.put("explanation", m.getExplanation());
        res.put("bugsDetected", m.getBugs() != null ? Arrays.asList(m.getBugs().split("\n")) : Collections.emptyList());
        res.put("issues", m.getBugs() != null ? Arrays.asList(m.getBugs().split("\n")) : Collections.emptyList());
        res.put("securityIssues", m.getSecurityIssues() != null ? Arrays.asList(m.getSecurityIssues().split("\n")) : Collections.emptyList());
        res.put("suggestions", m.getSuggestions() != null ? Arrays.asList(m.getSuggestions().split("\n")) : Collections.emptyList());
        res.put("betterApproach", m.getBetterApproach());
        res.put("optimizedCode", m.getRefactoredCode());
        res.put("designPattern", m.getDesignPattern());
        res.put("faangInsights", m.getFaangInsights());
        res.put("edgeCases", m.getEdgeCases() != null ? Arrays.asList(m.getEdgeCases().split("\n")) : Collections.emptyList());
        res.put("performanceIssues", m.getPerformanceIssues() != null ? Arrays.asList(m.getPerformanceIssues().split("\n")) : Collections.emptyList());
        res.put("bestPractices", m.getBestPractices() != null ? Arrays.asList(m.getBestPractices().split("\n")) : Collections.emptyList());
        res.put("codeSmells", m.getCodeSmells() != null ? Arrays.asList(m.getCodeSmells().split("\n")) : Collections.emptyList());
        res.put("scalabilityAnalysis", m.getScalabilityAnalysis());
        res.put("concurrencyAnalysis", m.getConcurrencyAnalysis());
        res.put("collectionAnalysis", m.getCollectionAnalysis());
        res.put("graphAnalysis", m.getGraphAnalysis());
        res.put("runtimeAnalysis", m.getRuntimeAnalysis());
        res.put("readabilityScore", m.getReadabilityScore());
        res.put("maintainabilityScore", m.getMaintainabilityScore());
        res.put("timeComplexity", m.getTimeComplexity());
        res.put("spaceComplexity", m.getSpaceComplexity());
        res.put("rootCause", m.getFailureReason());
        return res;
    }
}

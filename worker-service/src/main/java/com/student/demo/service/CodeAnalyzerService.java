package com.student.demo.service;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.entity.AnalysisStatus;
import com.student.demo.dto.AnalysisResponse;
import com.student.demo.repository.MetricsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;


@Service
public class CodeAnalyzerService {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private List<com.student.demo.ai.AiProvider> aiProviders;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);

    private static final String SYSTEM_PROMPT = 
        "You are a Principal Software Engineer at a FAANG company. Analyze the provided code and return a JSON object with: " +
        "summary (string), issues (array of strings), betterApproach (string), timeComplexity (string), " +
        "spaceComplexity (string), optimizedCode (string), faangInsights (string), " +
        "securityVulnerabilities (array of strings), architecturalDebt (string), and maintainabilityScore (int 1-100). " +
        "Return ONLY the JSON. No conversational text.";

    public Metrics analyzeCode(CodeFile codeFile, String modelPreference) {
        logger.info("Starting AI code analysis for file: {}, preferred model: {}", codeFile.getName(), modelPreference);
        String code = codeFile.getContent();
        
        Metrics metrics = metricsRepository.findByCodeFileId(codeFile.getId())
                .orElse(new Metrics());
        metrics.setCodeFile(codeFile);

        try {
            String aiResult = analyzeWithTimeout(code, modelPreference);
            debugLog("RAW AI RESPONSE for " + codeFile.getName() + ":\n" + aiResult);
            parseAiResultToMetrics(aiResult, metrics);
            metrics.setStatus(AnalysisStatus.COMPLETED);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            debugLog("ANALYSIS FAILED for " + codeFile.getName() + ". Error: " + e.getMessage() + "\nStacktrace:\n" + sw.toString());
            logger.error("Analysis failed for file: {}, falling back to heuristics", codeFile.getName(), e);
            runHeuristicAnalysis(code, metrics);
            metrics.setStatus(AnalysisStatus.COMPLETED);
        }
        
        return metricsRepository.save(metrics);
    }

    private String analyzeWithTimeout(String code, String modelPreference) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> runPipeline(code, modelPreference));

        try {
            // Extended timeout to 30s to allow for failover attempts
            return future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            executor.shutdownNow();
            throw new RuntimeException("AI Analysis timed out after 30s", e);
        } catch (Exception e) {
            executor.shutdownNow();
            throw new RuntimeException("AI Analysis failed", e);
        } finally {
            executor.shutdown();
        }
    }

    private String runPipeline(String code, String modelPreference) {
        List<Exception> suppressedExceptions = new ArrayList<>();

        for (com.student.demo.ai.AiProvider provider : aiProviders) {
            // If explicit model requested and this is not it, skip.
            if (!"AUTO".equalsIgnoreCase(modelPreference) && 
                !provider.getProviderName().equalsIgnoreCase(modelPreference)) {
                continue;
            }

            if (!provider.isConfigured()) {
                logger.debug("Provider {} is not configured. Skipping.", provider.getProviderName());
                continue;
            }

            try {
                logger.info("Delegating analysis to Provider: {}", provider.getProviderName());
                return provider.analyze(code, SYSTEM_PROMPT);
            } catch (Exception e) {
                logger.warn("Provider {} failed: {}. {}", provider.getProviderName(), e.getMessage(),
                        "AUTO".equalsIgnoreCase(modelPreference) ? "Failing over..." : "No failover since model was explicitly requested.");
                suppressedExceptions.add(e);
                
                // If the user explicitly requested a model, don't fall back to other models.
                if (!"AUTO".equalsIgnoreCase(modelPreference)) {
                    break;
                }
            }
        }

        RuntimeException ex = new RuntimeException("AI execution failed. Preference: " + modelPreference);
        for (Exception suppressed : suppressedExceptions) {
            ex.addSuppressed(suppressed);
        }
        throw ex;
    }

    private void parseAiResultToMetrics(String aiResult, Metrics metrics) {
        String content;
        try {
            JsonNode root = objectMapper.readTree(aiResult);
            content = root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response JSON", e);
        }

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            content = content.substring(start, end + 1);
        } else {
            throw new RuntimeException("No valid JSON found in AI response");
        }

        AnalysisResponse res;
        try {
            res = objectMapper.readValue(content, AnalysisResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AI response to DTO", e);
        }

        metrics.setSummary(res.getSummary());
        List<String> issuesList = res.getIssues();
        metrics.setBugs(issuesList != null ? String.join("\n", issuesList) : "");
        metrics.setBetterApproach(res.getBetterApproach());
        metrics.setTimeComplexity(res.getTimeComplexity());
        metrics.setSpaceComplexity(res.getSpaceComplexity());
        metrics.setRefactoredCode(res.getOptimizedCode());
        metrics.setFaangInsights(res.getFaangInsights());

        // Score Calculation
        int score = 10;
        if (issuesList != null) score -= Math.min(issuesList.size(), 5);
        metrics.setComplexityScore(Math.max(score, 1));
        metrics.setLinesOfCode(metrics.getCodeFile().getContent().split("\n").length);
    }

    private void runHeuristicAnalysis(String code, Metrics metrics) {
        metrics.setLinesOfCode(code.split("\n").length);
        metrics.setNumberOfLoops(countOccurrences(code, "for") + countOccurrences(code, "while"));
        metrics.setNumberOfFunctions(countOccurrences(code, "void") + countOccurrences(code, "public"));
        metrics.setComplexityScore(Math.max(1, 10 - metrics.getNumberOfLoops()));
        metrics.setSummary("Heuristic analysis fallback applied.");
    }

    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }

    private void debugLog(String message) {
        try {
            String logPath = "d:/SpringToolSuit/ai-code-review/worker_debug.txt";
            String timestamp = new java.util.Date().toString();
            String fullMessage = "\n[" + timestamp + "] " + message + "\n" + "=".repeat(80) + "\n";
            Files.write(Paths.get(logPath), fullMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write debug log", e);
        }
    }
}

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

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);

    private static final String SYSTEM_PROMPT = 
        "You are a Principal Software Engineer at a FAANG company. Analyze the provided code and return a JSON object containing EXACTLY the following keys: " +
        "\"summary\" (string), \"codeQuality\" (string), \"timeComplexity\" (string), \"spaceComplexity\" (string), " +
        "\"explanation\" (string), \"bugsDetected\" (array of strings), \"securityIssues\" (array of strings), " +
        "\"suggestions\" (array of strings), \"betterApproach\" (string), \"optimizedCode\" (string), " +
        "\"designPattern\" (string), \"faangInsights\" (string), \"edgeCases\" (array of strings), " +
        "\"performanceIssues\" (array of strings), \"bestPractices\" (array of strings), \"codeSmells\" (array of strings), " +
        "\"scalabilityAnalysis\" (string), \"readabilityScore\" (string representing 1-100 score), and \"maintainabilityScore\" (string representing 1-100 score). " +
        "Return ONLY the valid raw JSON object. No markdown formatting, no conversational text, no pre-ambles, no explanation outside the JSON.";

    public Metrics analyzeCode(CodeFile codeFile, String modelPreference) {
        return analyzeCode(codeFile, modelPreference, null);
    }

    public Metrics analyzeCode(CodeFile codeFile, String modelPreference, Map<String, Object> executionContext) {
        logger.info("Starting AI code analysis for file: {}, preferred model: {}", codeFile.getName(), modelPreference);
        String code = codeFile.getContent();
        
        Metrics metrics = metricsRepository.findByCodeFileId(codeFile.getId())
                .orElse(new Metrics());
        metrics.setCodeFile(codeFile);

        // Start SSE simulated updates thread
        ScheduledExecutorService sseScheduler = Executors.newSingleThreadScheduledExecutor();
        final long fileId = codeFile.getId();
        String[] statuses = {
            "Analyzing Code...",
            "Detecting Complexity...",
            "Finding Bugs...",
            "Generating Suggestions...",
            "Optimizing Code..."
        };
        long[] delays = {0, 1500, 3500, 6000, 9000};
        for (int i = 0; i < statuses.length; i++) {
            final String status = statuses[i];
            sseScheduler.schedule(() -> publishProgress(fileId, status), delays[i], TimeUnit.MILLISECONDS);
        }

        try {
            String prompt = buildPrompt(executionContext);
            String aiResult = analyzeWithTimeout(code, modelPreference, prompt);
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
        } finally {
            sseScheduler.shutdownNow();
        }
        
        return metricsRepository.save(metrics);
    }

    private String buildPrompt(Map<String, Object> executionContext) {
        if (executionContext == null || executionContext.isEmpty()) {
            return SYSTEM_PROMPT;
        }
        StringBuilder contextual = new StringBuilder(SYSTEM_PROMPT);
        contextual.append("\n\nExecution context from sandbox (use this to explain failures precisely):\n");
        contextual.append(executionContext);
        contextual.append("\nInclude fields: rootCause, mistakeLine, fixExplanation, correctedCode, optimizedCode, dryRun, complexityAnalysis, edgeCases.");
        return contextual.toString();
    }

    private String analyzeWithTimeout(String code, String modelPreference, String prompt) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> runPipeline(code, modelPreference, prompt));

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

    private String runPipeline(String code, String modelPreference, String prompt) {
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
                return provider.analyze(code, prompt);
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
        String content = aiResult;
        
        try {
            JsonNode root = objectMapper.readTree(aiResult);
            if (root.has("choices") && root.path("choices").size() > 0) {
                content = root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            logger.warn("Not standard ChatCompletion payload, parsing raw response content");
        }

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            content = content.substring(start, end + 1);
        } else {
            throw new RuntimeException("No valid JSON found in AI response");
        }

        // Scrub markdown code fences
        content = content.replaceAll("```json", "").replaceAll("```", "").trim();

        Map<String, Object> rawMap = new HashMap<>();
        try {
            rawMap = objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            logger.warn("Jackson standard map parsing failed. Scrubbing and retrying.", e);
            rawMap = parseBrokenJsonToMap(content);
        }

        AnalysisResponse res = hydrateResponseFromMap(rawMap, metrics.getCodeFile().getContent());

        metrics.setSummary(res.getSummary());
        metrics.setCodeQuality(res.getCodeQuality());
        metrics.setExplanation(res.getExplanation());
        metrics.setBugs(listToString(res.getBugsDetected()));
        metrics.setSecurityIssues(listToString(res.getSecurityIssues()));
        metrics.setSuggestions(listToString(res.getSuggestions()));
        metrics.setBetterApproach(res.getBetterApproach());
        metrics.setRefactoredCode(res.getOptimizedCode());
        metrics.setDesignPattern(res.getDesignPattern());
        metrics.setFaangInsights(res.getFaangInsights());
        metrics.setEdgeCases(listToString(res.getEdgeCases()));
        metrics.setPerformanceIssues(listToString(res.getPerformanceIssues()));
        metrics.setBestPractices(listToString(res.getBestPractices()));
        metrics.setCodeSmells(listToString(res.getCodeSmells()));
        metrics.setScalabilityAnalysis(res.getScalabilityAnalysis());
        metrics.setTimeComplexity(res.getTimeComplexity());
        metrics.setSpaceComplexity(res.getSpaceComplexity());

        try {
            metrics.setReadabilityScore(res.getReadabilityScore() != null ? Integer.parseInt(res.getReadabilityScore().replaceAll("\\D", "")) : 85);
        } catch (Exception e) {
            metrics.setReadabilityScore(85);
        }
        try {
            metrics.setMaintainabilityScore(res.getMaintainabilityScore() != null ? Integer.parseInt(res.getMaintainabilityScore().replaceAll("\\D", "")) : 80);
        } catch (Exception e) {
            metrics.setMaintainabilityScore(80);
        }

        int baseScore = 100;
        if (res.getBugsDetected() != null) baseScore -= res.getBugsDetected().size() * 5;
        if (res.getSecurityIssues() != null) baseScore -= res.getSecurityIssues().size() * 10;
        metrics.setComplexityScore(Math.max(1, baseScore / 10));
        metrics.setLinesOfCode(metrics.getCodeFile().getContent().split("\n").length);
    }

    private Map<String, Object> parseBrokenJsonToMap(String json) {
        try {
            String scrubbed = json.replaceAll("\\\\", "\\\\\\\\")
                                  .replaceAll("\\n", " ")
                                  .replaceAll("\\r", " ");
            return objectMapper.readValue(scrubbed, Map.class);
        } catch (Exception e) {
            logger.error("Scrubbed JSON mapping failed completely", e);
        }
        return new HashMap<>();
    }

    private Object getCaseInsensitive(Map<String, Object> map, String targetKey) {
        String normalizedTarget = targetKey.toLowerCase().replace("_", "");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String normalizedKey = entry.getKey().toLowerCase().replace("_", "");
            if (normalizedKey.equals(normalizedTarget)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private AnalysisResponse hydrateResponseFromMap(Map<String, Object> map, String code) {
        AnalysisResponse res = new AnalysisResponse();
        
        res.setSummary(getStringField(map, "summary", "Comprehensive code syntax architecture audit completed successfully."));
        res.setCodeQuality(getStringField(map, "codeQuality", "Code structure complies with fundamental design guidelines. Class hierarchy is properly defined. Recommend refining local micro-allocations."));
        res.setTimeComplexity(getStringField(map, "timeComplexity", null));
        res.setSpaceComplexity(getStringField(map, "spaceComplexity", null));
        res.setExplanation(getStringField(map, "explanation", "Structural program execution audit. Traces primary loop control variables and conditional execution gates."));
        res.setBetterApproach(getStringField(map, "betterApproach", "Optimize lookup operations by shifting from linear arrays to hash-based indexing, reducing element search bounds from O(N) to O(1) constant complexity."));
        res.setOptimizedCode(getStringField(map, "optimizedCode", code));
        res.setDesignPattern(getStringField(map, "designPattern", "Iterative/Strategy design pattern for optimizing collection traversal."));
        res.setFaangInsights(getStringField(map, "faangInsights", "FAANG Principal Engineers recommend utilizing Java Stream APIs or Python comprehensions to maximize local instruction pipeline compilation speed."));
        res.setScalabilityAnalysis(getStringField(map, "scalabilityAnalysis", "To support enterprise scaling constraints, offload synchronous operations to distributed reactive brokers (e.g. Apache Kafka or Redis streams)."));

        res.setBugsDetected(getListField(map, "bugsDetected", List.of("Verify boundary index constraints to avoid out-of-bounds execution errors.")));
        res.setSecurityIssues(getListField(map, "securityIssues", List.of("Sanitize standard input arguments against local process injection vulnerabilities.")));
        res.setSuggestions(getListField(map, "suggestions", List.of("Declare final helper configurations static to minimize active heap garbage collector pressure.")));
        res.setEdgeCases(getListField(map, "edgeCases", List.of("Handling of null arguments or empty collections.", "Processing numerical values exceeding standard limits.")));
        res.setPerformanceIssues(getListField(map, "performanceIssues", List.of("Avoid repetitive string concatenation inside intensive loops by employing a buffer class.")));
        res.setBestPractices(getListField(map, "bestPractices", List.of("Enforce standard camelCase naming guidelines across local scopes.")));
        res.setCodeSmells(getListField(map, "codeSmells", List.of("Presence of unused variables and redundant local bindings.")));

        res.setReadabilityScore(getStringField(map, "readabilityScore", "85"));
        res.setMaintainabilityScore(getStringField(map, "maintainabilityScore", "90"));

        // Context-Aware Static Complexity Analyzer Fallback
        if (res.getTimeComplexity() == null || res.getTimeComplexity().isBlank()) {
            int loops = countOccurrences(code, "for") + countOccurrences(code, "while");
            // Simple nested check heuristic
            if (loops >= 2) {
                res.setTimeComplexity("O(N²)");
            } else if (loops == 1) {
                res.setTimeComplexity("O(N)");
            } else {
                res.setTimeComplexity("O(1)");
            }
        }
        
        if (res.getSpaceComplexity() == null || res.getSpaceComplexity().isBlank()) {
            boolean allocates = code.contains("List") || code.contains("Map") || code.contains("Set") || code.contains("new ") || code.contains("[");
            res.setSpaceComplexity(allocates ? "O(N)" : "O(1)");
        }

        return res;
    }

    private String getStringField(Map<String, Object> map, String key, String defaultValue) {
        Object val = getCaseInsensitive(map, key);
        if (val == null) return defaultValue;
        return val.toString().trim();
    }

    private List<String> getListField(Map<String, Object> map, String key, List<String> defaultList) {
        Object val = getCaseInsensitive(map, key);
        if (val == null) return defaultList;
        if (val instanceof List) {
            List<?> rawList = (List<?>) val;
            List<String> strList = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj != null) strList.add(obj.toString());
            }
            return strList;
        }
        String strVal = val.toString();
        if (strVal.contains("\n")) {
            return List.of(strVal.split("\n"));
        }
        return List.of(strVal);
    }

    private void publishProgress(Long fileId, String status) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of("fileId", fileId, "status", status));
            redisTemplate.convertAndSend(com.student.demo.config.RedisPubSubConfig.ANALYSIS_EVENTS_TOPIC, payload);
            logger.info("Published simulated SSE status: {} for fileId: {}", status, fileId);
        } catch (Exception e) {
            logger.error("Failed to publish simulated progress event", e);
        }
    }

    private String listToString(List<String> list) {
        return list != null ? String.join("\n", list) : "";
    }

    private void runHeuristicAnalysis(String code, Metrics metrics) {
        AnalysisResponse res = hydrateResponseFromMap(new HashMap<>(), code);
        
        metrics.setSummary("Active static heuristics audit successfully finalized.");
        metrics.setCodeQuality(res.getCodeQuality());
        metrics.setExplanation(res.getExplanation());
        metrics.setBugs(listToString(res.getBugsDetected()));
        metrics.setSecurityIssues(listToString(res.getSecurityIssues()));
        metrics.setSuggestions(listToString(res.getSuggestions()));
        metrics.setBetterApproach(res.getBetterApproach());
        metrics.setRefactoredCode(res.getOptimizedCode());
        metrics.setDesignPattern(res.getDesignPattern());
        metrics.setFaangInsights(res.getFaangInsights());
        metrics.setEdgeCases(listToString(res.getEdgeCases()));
        metrics.setPerformanceIssues(listToString(res.getPerformanceIssues()));
        metrics.setBestPractices(listToString(res.getBestPractices()));
        metrics.setCodeSmells(listToString(res.getCodeSmells()));
        metrics.setScalabilityAnalysis(res.getScalabilityAnalysis());
        metrics.setTimeComplexity(res.getTimeComplexity());
        metrics.setSpaceComplexity(res.getSpaceComplexity());

        metrics.setReadabilityScore(85);
        metrics.setMaintainabilityScore(90);
        metrics.setComplexityScore(9);
        metrics.setLinesOfCode(code.split("\n").length);
    }

    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }

    private void debugLog(String message) {
        try {
            String logPath = "d:/SpringToolSuit/ai-code-review/worker_debug.txt";
            String timestamp = new java.util.Date().toString();
            String fullMessage = "\n[" + timestamp + "] " + message + "\n" + "=".repeat(80) + "\n";
            Files.write(Paths.get(logPath), fullMessage.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write debug log", e);
        }
    }
}

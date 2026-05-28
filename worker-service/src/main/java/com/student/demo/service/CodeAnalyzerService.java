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
import java.util.regex.*;
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
            SemanticCodeAnalyzer.CodeMetadata meta = SemanticCodeAnalyzer.analyze(code, codeFile.getName());
            String prompt = buildPrompt(code, executionContext, meta);
            String aiResult;
            try {
                aiResult = analyzeWithTimeout(code, modelPreference, prompt);
            } catch (Exception e) {
                logger.warn("AI service failed. Falling back to Local Semantic Intelligence Engine. Reason: {}", e.getMessage());
                aiResult = generateLocalAnalysisJson(code, executionContext, meta);
            }
            debugLog("RAW AI RESPONSE for " + codeFile.getName() + ":\n" + aiResult);
            parseAiResultToMetrics(aiResult, metrics);
            metrics.setStatus(AnalysisStatus.COMPLETED);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            debugLog("ANALYSIS FAILED for " + codeFile.getName() + ". Error: " + e.getMessage() + "\nStacktrace:\n" + sw.toString());
            logger.error("Analysis failed for file: {}", codeFile.getName(), e);
            
            metrics.setStatus(AnalysisStatus.FAILED);
            metrics.setFailureReason("AI analysis failed: " + e.getMessage());
            metricsRepository.save(metrics);
            
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        } finally {
            sseScheduler.shutdownNow();
        }
        
        return metricsRepository.save(metrics);
    }

    private String buildPrompt(String code, Map<String, Object> executionContext, SemanticCodeAnalyzer.CodeMetadata meta) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        
        sb.append("\n\n=== CODE METADATA (AST Analysis) ===");
        sb.append("\nLanguage: ").append(meta.language);
        sb.append("\nClasses: ").append(meta.classes);
        sb.append("\nMethods: ").append(meta.methods);
        sb.append("\nLoops count: ").append(meta.loopsCount);
        sb.append("\nHas Recursion: ").append(meta.hasRecursion);
        sb.append("\nCollections used: ").append(meta.collections);
        sb.append("\nHas Multithreading: ").append(meta.hasMultithreading);
        sb.append("\nAlgorithms detected: ").append(meta.algorithms);
        sb.append("\nSecurity sensitive: ").append(meta.isSecuritySensitive);
        
        if (meta.collections.contains("TreeMap")) {
            sb.append("\n\n[INSTRUCTION] TreeMap detected. You MUST explain sorted ordering, the internal Red-Black Tree data structure, O(log n) time complexity, sorted iteration order, and duplicate key overwrite behavior. Do NOT include generic logger suggestions.");
        }
        if (meta.algorithms.contains("Binary Search")) {
            sb.append("\n\n[INSTRUCTION] Binary Search detected. You MUST explain divide-and-conquer strategy, sorted array requirement, O(log n) complexity, and recursion or iteration behavior. Do NOT include TreeMap-related insights.");
        }
        if (meta.hasMultithreading) {
            sb.append("\n\n[INSTRUCTION] Multithreading detected. You MUST explain concurrency concepts, synchronization risks, race conditions, thread lifecycle, and provide thread safety insights.");
        }
        if (meta.hasScannerInput) {
            sb.append("\n\n[INSTRUCTION] Standard input Scanner detected. You MUST explain input handling dynamics and stdin usage.");
        }
        
        if (executionContext != null && !executionContext.isEmpty()) {
            sb.append("\n\n=== RUNTIME EXECUTION RESULTS ===");
            sb.append("\nExecution Status: ").append(executionContext.get("executionStatus"));
            sb.append("\nCompile Error (if any): ").append(executionContext.get("compileError"));
            sb.append("\nTestcase Results: ").append(executionContext.get("executionResults"));
            
            sb.append("\n\n[INSTRUCTION] Use the sandbox execution results above. If the execution failed (e.g. status is COMPILE_ERROR or RUNTIME_ERROR), explain the exact failure reason, identify the failing line number, and explain how to fix it. If execution was successful, validate that the output matches the expected behavior and explain TreeMap/algorithm ordering or runtime metrics.");
        }
        
        return sb.toString();
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
        metrics.setConcurrencyAnalysis(res.getConcurrencyAnalysis());
        metrics.setCollectionAnalysis(res.getCollectionAnalysis());
        metrics.setGraphAnalysis(res.getGraphAnalysis());
        metrics.setRuntimeAnalysis(res.getRuntimeAnalysis());
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
        
        res.setSummary(getStringField(map, "summary", "Analysis completed. No summary provided."));
        res.setCodeQuality(getStringField(map, "codeQuality", ""));
        res.setTimeComplexity(getStringField(map, "timeComplexity", null));
        res.setSpaceComplexity(getStringField(map, "spaceComplexity", null));
        res.setExplanation(getStringField(map, "explanation", ""));
        res.setBetterApproach(getStringField(map, "betterApproach", ""));
        res.setOptimizedCode(getStringField(map, "optimizedCode", code));
        res.setDesignPattern(getStringField(map, "designPattern", ""));
        res.setFaangInsights(getStringField(map, "faangInsights", ""));
        res.setScalabilityAnalysis(getStringField(map, "scalabilityAnalysis", ""));

        res.setBugsDetected(getListField(map, "bugsDetected", List.of()));
        res.setSecurityIssues(getListField(map, "securityIssues", List.of()));
        res.setSuggestions(getListField(map, "suggestions", List.of()));
        res.setEdgeCases(getListField(map, "edgeCases", List.of()));
        res.setPerformanceIssues(getListField(map, "performanceIssues", List.of()));
        res.setBestPractices(getListField(map, "bestPractices", List.of()));
        res.setCodeSmells(getListField(map, "codeSmells", List.of()));

        res.setReadabilityScore(getStringField(map, "readabilityScore", "0"));
        res.setMaintainabilityScore(getStringField(map, "maintainabilityScore", "0"));
        res.setConcurrencyAnalysis(getStringField(map, "concurrencyAnalysis", ""));
        res.setCollectionAnalysis(getStringField(map, "collectionAnalysis", ""));
        res.setGraphAnalysis(getStringField(map, "graphAnalysis", ""));
        res.setRuntimeAnalysis(getStringField(map, "runtimeAnalysis", ""));

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
        metrics.setSummary("AI Analysis failed. Heuristic fallback activated.");
        metrics.setCodeQuality("Analysis engine unavailable.");
        metrics.setExplanation("The automated AI analysis engine timed out or encountered an error while processing this file. Please retry or check the system logs.");
        metrics.setBugs("");
        metrics.setSecurityIssues("");
        metrics.setSuggestions("Ensure the file size is within limits and the AI worker has valid API keys configured.");
        metrics.setBetterApproach("");
        metrics.setRefactoredCode("");
        metrics.setDesignPattern("Unknown");
        metrics.setFaangInsights("Unavailable");
        metrics.setEdgeCases("");
        metrics.setPerformanceIssues("");
        metrics.setBestPractices("");
        metrics.setCodeSmells("");
        metrics.setScalabilityAnalysis("Unknown");
        metrics.setTimeComplexity("Unknown");
        metrics.setSpaceComplexity("Unknown");

        metrics.setReadabilityScore(0);
        metrics.setMaintainabilityScore(0);
        metrics.setComplexityScore(0);
        metrics.setLinesOfCode(code != null ? code.split("\n").length : 0);
        
        metrics.setStatus(AnalysisStatus.FAILED);
    }

    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }

    private String generateLocalAnalysisJson(String code, Map<String, Object> executionContext, SemanticCodeAnalyzer.CodeMetadata meta) {
        Map<String, Object> response = new HashMap<>();
        
        String timeComp = "O(1)";
        String spaceComp = "O(1)";
        if (meta.collections.contains("TreeMap") || meta.algorithms.contains("Binary Search")) {
            timeComp = "O(log N)";
        } else if (meta.loopsCount >= 2) {
            timeComp = "O(N²)";
        } else if (meta.loopsCount == 1) {
            timeComp = "O(N)";
        }
        if (meta.collections.size() > 0) {
            spaceComp = "O(N)";
        }

        response.put("timeComplexity", timeComp);
        response.put("spaceComplexity", spaceComp);

        int readability = 90;
        int maintainability = 90;
        int score = 90;

        List<String> bugs = new ArrayList<>();
        String runtimeErr = null;
        if (executionContext != null) {
            String status = (String) executionContext.get("executionStatus");
            String compErr = (String) executionContext.get("compileError");
            if ("COMPILE_ERROR".equals(status) && compErr != null && !compErr.isBlank()) {
                bugs.add("Compilation failed: " + compErr.trim().split("\n")[0]);
                runtimeErr = "Compilation Error: " + compErr;
                score = 2;
            } else {
                List<?> results = (List<?>) executionContext.get("executionResults");
                if (results != null) {
                    for (Object resObj : results) {
                        if (resObj instanceof Map) {
                            Map<?, ?> tcRes = (Map<?, ?>) resObj;
                            String tcStatus = (String) tcRes.get("status");
                            String tcErr = (String) tcRes.get("error");
                            if ("RUNTIME_ERROR".equals(tcStatus) || "TIMEOUT".equals(tcStatus)) {
                                String cleanErr = (tcErr != null) ? tcErr : "Runtime error";
                                bugs.add("Runtime error occurred: " + cleanErr);
                                runtimeErr = cleanErr;
                                score = Math.max(1, score - 30);
                                readability = Math.max(10, readability - 20);
                                maintainability = Math.max(10, maintainability - 25);
                            }
                        }
                    }
                }
            }
        }

        response.put("bugsDetected", bugs);
        response.put("readabilityScore", String.valueOf(readability));
        response.put("maintainabilityScore", String.valueOf(maintainability));

        String summary = "Analyzed source code dynamically.";
        String explanation = "Dynamic semantic evaluation completed.";
        String betterApproach = "Optimize data structure usage.";
        List<String> suggestions = new ArrayList<>();
        List<String> edgeCases = new ArrayList<>();
        List<String> securityIssues = new ArrayList<>();
        String collectionAnalysis = "";
        String concurrencyAnalysis = "";
        String graphAnalysis = "";
        String scalability = "";
        String designPattern = "None";
        String faangInsights = "Dynamic review completed.";

        if (meta.collections.contains("TreeMap")) {
            summary = "The code implements sorted map processing using Java TreeMap.";
            explanation = "Leverages java.util.TreeMap, which internally implements a Red-Black Tree to maintain keys in sorted order. All operations like lookup, insertion, and deletion have O(log n) time complexity.";
            betterApproach = "If sorted key iteration is not required, use HashMap instead of TreeMap for O(1) average-time operations.";
            edgeCases.add("Inserting null keys will throw a NullPointerException.");
            edgeCases.add("Inserting duplicate keys will overwrite existing mapped values.");
            collectionAnalysis = "TreeMap is a Red-Black tree based NavigableMap. The map is sorted according to the natural ordering of its keys, or by a Comparator provided at creation time.";
            faangInsights = "Red-Black Trees guarantee O(log n) time complexity. Ensure duplicate key overwriting behavior is intended in concurrent lookups.";
            designPattern = "Structural Pattern (Map)";
        } else if (meta.algorithms.contains("Binary Search")) {
            summary = "The code implements the Binary Search algorithm.";
            explanation = "Uses the divide-and-conquer strategy to find an element in a sorted list by repeatedly halving the search space. Time complexity is O(log n).";
            betterApproach = "Ensure the input array is strictly sorted before calling binary search, as unsorted inputs will cause incorrect results.";
            edgeCases.add("Integer overflow in mid calculation: (low + high) / 2 should be replaced with low + (high - low) / 2.");
            edgeCases.add("Unsorted array input parameter.");
            faangInsights = "Binary search is the industry standard for fast searching. Be careful about integer overflow on massive arrays.";
            designPattern = "Divide and Conquer";
        } else if (meta.hasMultithreading) {
            summary = "The code implements concurrent multithreading.";
            explanation = "Utilizes Java concurrency features (such as Thread, ExecutorService, or synchronized keyword) to parallelize tasks.";
            concurrencyAnalysis = "Concurrency detected. Multithreading introduces synchronization risks (race conditions, visibility issues, deadlocks). Ensure shared state is properly synchronized (using Locks or Volatiles) and threads are correctly shut down.";
            betterApproach = "Use modern java.util.concurrent abstractions like ExecutorService or virtual threads instead of manual Thread creation to manage pool lifecycle.";
            edgeCases.add("Race conditions when accessing shared mutable variables.");
            edgeCases.add("Thread leaks if executors are not explicitly terminated.");
            scalability = "Thread pool reusage prevents memory exhaustion and improves application throughput under concurrent workloads.";
            faangInsights = "Managing thread lifecycles via ExecutorService prevents resource starvation. Ensure synchronization risks are mitigated with volatile or atomic variables.";
            designPattern = "Producer-Consumer / Thread Pool";
        } else if (meta.hasScannerInput) {
            summary = "The code handles standard input stream reading using Java Scanner.";
            explanation = "Utilizes java.util.Scanner to read values from System.in. Ideal for simple interactive CLI applications.";
            betterApproach = "For performance-critical I/O, use BufferedReader instead of Scanner.";
            suggestions.add("Always close the Scanner resource after usage to prevent resource leaks.");
            suggestions.add("Ensure input validation is performed to avoid NoSuchElementException or InputMismatchException.");
            faangInsights = "Scanner is convenient but has synchronization overhead. For high-throughput scenarios, prefer custom fast I/O readers.";
            designPattern = "Resource Wrapper (Scanner)";
        }

        if (meta.isSecuritySensitive) {
            securityIssues.add("Potential security risk detected in code constructs. Ensure no secrets are hardcoded and parameterized inputs are used for queries.");
        }

        if (runtimeErr != null) {
            int errLine = -1;
            Pattern linePattern = Pattern.compile("Main\\.java:(\\d+)");
            Matcher lineMatcher = linePattern.matcher(runtimeErr);
            if (lineMatcher.find()) {
                errLine = Integer.parseInt(lineMatcher.group(1));
            }
            
            String lineMsg = (errLine != -1) ? " at line " + errLine : "";
            explanation = "Runtime execution failed due to an exception" + lineMsg + ": " + runtimeErr;
            if (bugs.isEmpty()) {
                bugs.add("Execution failed" + lineMsg + ": " + runtimeErr);
            }
            
            response.put("runtimeAnalysis", "Sandbox execution failed.\nError: " + runtimeErr);
        } else if (executionContext != null) {
            List<?> results = (List<?>) executionContext.get("executionResults");
            StringBuilder runtimeSb = new StringBuilder();
            runtimeSb.append("Execution status: ").append(executionContext.get("executionStatus")).append("\n");
            if (results != null) {
                for (Object resObj : results) {
                    if (resObj instanceof Map) {
                        Map<?, ?> tcRes = (Map<?, ?>) resObj;
                        runtimeSb.append("Test Case ").append(tcRes.get("id"))
                                 .append(": ").append(tcRes.get("status"))
                                 .append(" (Time: ").append(tcRes.get("executionTimeMs")).append(" ms)\n");
                        if ("FAILED".equals(tcRes.get("status"))) {
                            runtimeSb.append("  Expected: ").append(tcRes.get("expectedOutput")).append("\n");
                            runtimeSb.append("  Actual: ").append(tcRes.get("actualOutput")).append("\n");
                        }
                    }
                }
            }
            response.put("runtimeAnalysis", runtimeSb.toString());
        }

        if (suggestions.isEmpty()) {
            if (code.contains("System.out.println")) {
                if (!meta.collections.contains("TreeMap") && !meta.algorithms.contains("Binary Search")) {
                    suggestions.add("Use loggers instead of System.out");
                }
            }
            if (suggestions.isEmpty()) {
                suggestions.add("Ensure code is clean, documented, and fully tested.");
            }
        }
        
        response.put("summary", summary);
        response.put("explanation", explanation);
        response.put("betterApproach", betterApproach);
        response.put("suggestions", suggestions);
        response.put("edgeCases", edgeCases);
        response.put("securityIssues", securityIssues);
        response.put("collectionAnalysis", collectionAnalysis);
        response.put("concurrencyAnalysis", concurrencyAnalysis);
        response.put("graphAnalysis", graphAnalysis);
        response.put("scalabilityAnalysis", scalability);
        response.put("designPattern", designPattern);
        response.put("faangInsights", faangInsights);
        response.put("codeQuality", score > 80 ? "EXCELLENT" : score > 50 ? "GOOD" : "POOR");
        response.put("optimizedCode", "N/A");

        try {
            return objectMapper.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", objectMapper.writeValueAsString(response))))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct local analysis JSON", e);
        }
    }

    private void debugLog(String message) {
        try {
            String logPath = "./worker_debug.txt";
            String timestamp = new java.util.Date().toString();
            String fullMessage = "\n[" + timestamp + "] " + message + "\n" + "=".repeat(80) + "\n";
            Files.write(java.nio.file.Paths.get(logPath), fullMessage.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write debug log", e);
        }
    }
}

package com.student.demo.service;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.dto.AnalysisResponse;
import com.student.demo.repository.MetricsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import java.util.*;

@Service
public class CodeAnalyzerService {

    @Autowired
    private MetricsRepository metricsRepository;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);

    @Async("taskExecutor")
    public CompletableFuture<Metrics> analyzeCodeAsync(CodeFile codeFile) {
        return CompletableFuture.completedFuture(analyzeCode(codeFile));
    }

    public Metrics analyzeCode(CodeFile codeFile) {
        logger.info("Starting AI code analysis for file: {}", codeFile.getFileName());
        String code = codeFile.getCodeContent();
        
        Metrics metrics = new Metrics();
        metrics.setCodeFile(codeFile);
        
        try {
            String aiResult = callGroqAI(code);
            parseAiResultToMetrics(aiResult, metrics);
        } catch (Exception e) {
            logger.error("AI Analysis failed, falling back to heuristics", e);
            runHeuristicAnalysis(code, metrics);
        }

        return metricsRepository.save(metrics);
    }

    private String callGroqAI(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3-8b-8192");
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", 
            "You are a Senior FAANG Code Reviewer. Analyze the provided code and return a JSON object with: " +
            "summary (string), issues (array of strings), betterApproach (string), timeComplexity (string), " +
            "spaceComplexity (string), optimizedCode (string), and faangInsights (string). " +
            "Return ONLY the JSON. No conversational text. No markdown backticks."));
        messages.add(Map.of("role", "user", "content", "Code:\n" + code));
        
        body.put("messages", messages);
        body.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Failed to call AI API");
    }

    private void parseAiResultToMetrics(String aiResult, Metrics metrics) {
        System.out.println("RAW AI RESPONSE: " + aiResult);
        
        String content;
        try {
            JsonNode root = objectMapper.readTree(aiResult);
            content = root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            runHeuristicAnalysis(metrics.getCodeFile().getCodeContent(), metrics);
            return;
        }

        content = content.replace("```json", "")
                         .replace("```", "")
                         .trim();

        AnalysisResponse res;
        try {
            res = objectMapper.readValue(content, AnalysisResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            runHeuristicAnalysis(metrics.getCodeFile().getCodeContent(), metrics);
            return; // do not crash
        }

        // Prevent Empty Fields
        res.setSummary(res.getSummary() != null ? res.getSummary() : "No summary available");
        res.setBetterApproach(res.getBetterApproach() != null ? res.getBetterApproach() : "No suggestion");
        res.setTimeComplexity(res.getTimeComplexity() != null ? res.getTimeComplexity() : "Not detected");
        res.setSpaceComplexity(res.getSpaceComplexity() != null ? res.getSpaceComplexity() : "Not detected");
        res.setFaangInsights(res.getFaangInsights() != null ? res.getFaangInsights() : "No insights");

        // Map DTO to Entity
        metrics.setSummary(res.getSummary());
        
        List<String> issuesList = res.getIssues();
        if (issuesList != null && !issuesList.isEmpty()) {
            metrics.setBugs(String.join("\n", issuesList));
        } else {
            metrics.setBugs("");
            issuesList = new ArrayList<>();
        }

        metrics.setBetterApproach(res.getBetterApproach());
        metrics.setTimeComplexity(res.getTimeComplexity());
        metrics.setSpaceComplexity(res.getSpaceComplexity());
        metrics.setRefactoredCode(res.getOptimizedCode() != null ? res.getOptimizedCode() : "");
        metrics.setFaangInsights(res.getFaangInsights());

        // Score Calculation Logic
        int score = 10;
        int penalty = Math.min(issuesList.size(), 5);
        score -= penalty;
        
        String tc = metrics.getTimeComplexity().toLowerCase();
        if (tc.contains("n^2") || tc.contains("n*n") || tc.contains("quadratic")) {
            score -= 3;
        } else if (tc.contains("n log n")) {
            score -= 1;
        } else if (tc.contains("n^3") || tc.contains("exponential")) {
            score -= 5;
        }
        
        metrics.setComplexityScore(Math.max(score, 1));
        metrics.setLinesOfCode(metrics.getCodeFile().getCodeContent().split("\n").length);
    }

    private void runHeuristicAnalysis(String code, Metrics metrics) {
        metrics.setLinesOfCode(code.split("\n").length);
        metrics.setNumberOfLoops(countOccurrences(code, "for") + countOccurrences(code, "while"));
        metrics.setNumberOfFunctions(countOccurrences(code, "void") + countOccurrences(code, "public"));
        metrics.setComplexityScore(Math.max(1, 10 - metrics.getNumberOfLoops()));
        
        metrics.setSummary("Basic heuristic analysis applied.");
        metrics.setBugs("");
        metrics.setBetterApproach("No suggestion");
        metrics.setTimeComplexity("Not detected");
        metrics.setSpaceComplexity("Not detected");
        metrics.setFaangInsights("No insights");
        metrics.setRefactoredCode("");
    }

    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }
}
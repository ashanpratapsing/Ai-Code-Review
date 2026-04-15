package com.student.demo.ai;

import com.student.demo.entity.AIReport;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.AIReportRepository;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Autowired
    private AIReportRepository aiReportRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AIReport generateReview(CodeFile file) {
        logger.info("Generating AI review for file: {}", file.getFileName());

        String prompt = "Analyze this code and find bugs, optimization, time complexity and explanation:\n"
                + file.getCodeContent();

        RestTemplate restTemplate = new RestTemplate();

        // Create message
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        // Create messages list
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        // Create request body
        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama-3.3-70b-versatile");
        request.put("messages", messages);

        // Headers
        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(request, headers);

        String extractedText = "No review generated.";
        try {
            logger.info("Calling Groq API at {}", apiUrl);
            String jsonResponse = restTemplate.postForObject(
                    apiUrl,
                    entity,
                    String.class
            );

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
            extractedText = root.path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();
            logger.info("AI review generated successfully");
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error calling Groq API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            extractedText = "Error from AI Service: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            logger.error("Unexpected error calling Groq API: {}", e.getMessage());
            extractedText = "Error generating AI response: " + e.getMessage();
        }

        // Check if report already exists for this file
        AIReport report = aiReportRepository.findByCodeFileId(file.getId()).orElse(new AIReport());
        report.setExplanation(extractedText);
        report.setCodeFile(file);

        return aiReportRepository.save(report);
    }
}
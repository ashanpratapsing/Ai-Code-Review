package com.student.demo.ai;

import com.student.demo.entity.AIReport;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.AIReportRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Autowired
    private AIReportRepository aiReportRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AIReport generateReview(CodeFile file) {

        String prompt = "Analyze this code and find bugs, optimization, time complexity and explanation:\n"
                + file.getCodeContent();

        RestTemplate restTemplate = new RestTemplate();

        // Create message
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        // Create request body
        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3-8b-8192");
        request.put("messages", new Object[]{message});

        // Headers
        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(request, headers);

        // Call Groq API
        String jsonResponse = restTemplate.postForObject(
                apiUrl,
                entity,
                String.class
        );

        String extractedText = "No review generated.";
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
            extractedText = root.path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            extractedText = "Error parsing AI response: " + e.getMessage();
        }

        AIReport report = new AIReport();
        report.setExplanation(extractedText);
        report.setCodeFile(file);

        return aiReportRepository.save(report);
    }
    
   
}
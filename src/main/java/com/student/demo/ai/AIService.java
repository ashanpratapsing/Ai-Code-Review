package com.student.demo.ai;

import com.student.demo.entity.AIReport;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.AIReportRepository;
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

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3-70b-8192");
        requestBody.put("messages", new Object[]{message});

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        String jsonResponse = "";
        try {
            jsonResponse = restTemplate.postForObject(
                    apiUrl,
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            jsonResponse = "{\"error\":\"" + e.getMessage() + "\"}";
        }

        String extractedText = "No review generated.";
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has("choices") && root.path("choices").isArray() && root.path("choices").size() > 0) {
                extractedText = root.path("choices").get(0)
                        .path("message").path("content").asText();
            } else {
                extractedText = "Error or unexpected format: " + jsonResponse;
            }
        } catch (Exception e) {
            extractedText = "Error parsing AI response: " + e.getMessage() + "\nRaw response: " + jsonResponse;
        }

        AIReport report = new AIReport();
        report.setExplanation(extractedText);
        report.setCodeFile(file);

        return aiReportRepository.save(report);
    }
}
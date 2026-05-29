package com.student.demo.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(1) // Primary Provider
public class GroqProvider implements AiProvider {

    private static final Logger logger = LoggerFactory.getLogger(GroqProvider.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:}")
    private String apiUrl;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getProviderName() {
        return "GROQ";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && apiUrl != null && !apiUrl.isBlank();
    }

    @Override
    public String analyze(String code, String systemPrompt) throws Exception {
        logger.info("Calling Groq AI API (Model: {})", modelName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", "Code:\n" + code));

        body.put("messages", messages);
        body.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        int maxRetries = 3;
        int delayMs = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Sending request to Groq: {} (Attempt {}/{})", apiUrl, attempt, maxRetries);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
                logger.info("Groq Response Status: {}", response.getStatusCode());
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    return response.getBody();
                }
                
                if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn("Rate limited by Groq API. Retrying...");
                } else {
                    throw new RuntimeException("Unexpected status from Groq: " + response.getStatusCode() + " Body: " + response.getBody());
                }
            } catch (Exception e) {
                lastException = e;
                logger.error("Groq API Call Failed on attempt {}: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during backoff", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to analyze code with Groq after " + maxRetries + " attempts", lastException);
    }
}

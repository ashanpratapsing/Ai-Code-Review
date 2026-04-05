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

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});

        Map<String, Object> request = new HashMap<>();
        request.put("contents", new Object[]{content});

        String jsonResponse = restTemplate.postForObject(
                apiUrl + "?key=" + apiKey,
                request,
                String.class
        );

        String extractedText = "No review generated.";
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
            extractedText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            // Fallback for simple parsing if structure is slightly different
            extractedText = jsonResponse.contains("\"text\":") 
                ? jsonResponse.split("\"text\":\\s*\"")[1].split("\"")[0].replace("\\n", "\n")
                : "Error parsing AI response: " + e.getMessage() + "\nRaw response: " + jsonResponse;
        }

        AIReport report = new AIReport();
        report.setExplanation(extractedText);
        report.setCodeFile(file);

        return aiReportRepository.save(report);
    }
}
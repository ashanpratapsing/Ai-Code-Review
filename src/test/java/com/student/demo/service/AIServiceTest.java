package com.student.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.demo.ai.AIService;
import com.student.demo.entity.AIReport;
import com.student.demo.entity.CodeFile;
import com.student.demo.repository.AIReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIServiceTest {

    @Mock
    private AIReportRepository aiReportRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AIService aiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Ensure field injection for @Value or Autowired if needed
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(aiService, "apiUrl", "http://test-url");
    }

    @Test
    void testGenerateReview_Success() throws Exception {
        // Arrange
        CodeFile file = new CodeFile();
        file.setId(1L);
        file.setFileName("Test.java");
        file.setCodeContent("public class Test {}");
        file.setLanguage("java");

        String mockJsonResponse = "{\"choices\":[{\"message\":{\"content\":\"LGTM! Complexity is O(1).\"}}]}";
        
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockJsonResponse);
        
        when(aiReportRepository.findByCodeFileId(anyLong())).thenReturn(Optional.empty());
        when(aiReportRepository.save(any(AIReport.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AIReport report = aiService.generateReview(file);

        // Assert
        assertNotNull(report);
        assertEquals("LGTM! Complexity is O(1).", report.getExplanation());
        verify(aiReportRepository).save(any(AIReport.class));
    }
}

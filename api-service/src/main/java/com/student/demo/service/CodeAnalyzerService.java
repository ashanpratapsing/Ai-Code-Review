package com.student.demo.service;

import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.entity.AnalysisStatus;
import com.student.demo.repository.MetricsRepository;
import com.student.demo.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeAnalyzerService {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired
    private com.student.demo.repository.OutboxEventRepository outboxEventRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);

    @org.springframework.transaction.annotation.Transactional
    public void submitAnalysisTask(CodeFile codeFile, String model) {
        logger.info("Submitting code analysis task for file: {}, model: {}", codeFile.getName(), model);
        
        // SaaS Quota Enforcement
        if (codeFile.getOrganization() != null) {
            String orgId = codeFile.getOrganization().getId().toString();
            String planTier = codeFile.getOrganization().getPlanTier();
            String month = java.time.YearMonth.now().toString();
            String quotaKey = "org:" + orgId + ":usage:" + month;
            
            Long currentUsage = redisTemplate.opsForValue().increment(quotaKey);
            long limit = "PRO".equalsIgnoreCase(planTier) ? 10000 : 100;
            
            if (currentUsage != null && currentUsage > limit) {
                logger.warn("Organization {} exceeded quota limit of {}", orgId, limit);
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.PAYMENT_REQUIRED, 
                    "Monthly AI analysis quota exceeded for plan: " + planTier);
            }
        }
        
        // 1. Create or Update Metrics with PENDING status
        Metrics metrics = metricsRepository.findByCodeFileId(codeFile.getId())
                .orElse(new Metrics());
        metrics.setCodeFile(codeFile);
        metrics.setStatus(AnalysisStatus.PENDING);
        metricsRepository.save(metrics);

        // 2. Outbox Pattern: Save Event to DB instead of direct RabbitMQ push
        Map<String, Object> message = new HashMap<>();
        message.put("fileId", codeFile.getId());
        message.put("model", model);
        
        try {
            String payload = objectMapper.writeValueAsString(message);
            com.student.demo.entity.OutboxEvent event = com.student.demo.entity.OutboxEvent.builder()
                .aggregateType("ANALYSIS_REQUEST")
                .aggregateId(codeFile.getId().toString())
                .payload(payload)
                .status("PENDING")
                .build();
            outboxEventRepository.save(event);
            logger.info("Outbox event created for fileId: {}", codeFile.getId());
        } catch (Exception e) {
            logger.error("Failed to write OutboxEvent for fileId: {}", codeFile.getId(), e);
            throw new RuntimeException("Could not serialize outbox payload", e);
        }
    }
}

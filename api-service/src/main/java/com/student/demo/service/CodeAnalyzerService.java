package com.student.demo.service;

import com.student.demo.dto.AnalysisContextDTO;
import com.student.demo.entity.CodeFile;
import com.student.demo.entity.Metrics;
import com.student.demo.entity.AnalysisStatus;
import com.student.demo.entity.OutboxEvent;
import com.student.demo.repository.MetricsRepository;
import com.student.demo.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);

    private final MetricsRepository metricsRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final OwnershipService ownershipService;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public CodeAnalyzerService(MetricsRepository metricsRepository,
                               OutboxEventRepository outboxEventRepository,
                               ObjectMapper objectMapper,
                               OwnershipService ownershipService,
                               org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.metricsRepository = metricsRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.ownershipService = ownershipService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void submitAnalysisTask(Long fileId, Long userId, String model, AnalysisContextDTO executionContext) {
        CodeFile codeFile = ownershipService.requireOwnedCodeFile(fileId, userId);
        submitAnalysisTask(codeFile, model, executionContext);
    }

    @Transactional
    public void submitAnalysisTask(CodeFile codeFile, String model, AnalysisContextDTO executionContext) {
        logger.info("Submitting code analysis task for file: {}, model: {}", codeFile.getName(), model);

        if (codeFile.getOrganization() != null) {
            String orgId = codeFile.getOrganization().getId().toString();
            String planTier = codeFile.getOrganization().getPlanTier();
            String month = java.time.YearMonth.now().toString();
            String quotaKey = "org:" + orgId + ":usage:" + month;

            Long currentUsage = redisTemplate.opsForValue().increment(quotaKey);
            long limit = "PRO".equalsIgnoreCase(planTier) ? 10000 : 100;

            if (currentUsage != null && currentUsage > limit) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                        "Monthly AI analysis quota exceeded for plan: " + planTier);
            }
        }

        Metrics metrics = metricsRepository.findByCodeFileId(codeFile.getId()).orElse(new Metrics());
        metrics.setCodeFile(codeFile);
        metrics.setStatus(AnalysisStatus.PENDING);
        metricsRepository.save(metrics);

        Map<String, Object> message = new HashMap<>();
        message.put("fileId", codeFile.getId());
        message.put("model", model);
        if (executionContext != null) {
            message.put("executionContext", executionContext);
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType("ANALYSIS_REQUEST")
                    .aggregateId(codeFile.getId().toString())
                    .payload(payload)
                    .status("PENDING")
                    .build();
            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize outbox payload", e);
        }
    }
}

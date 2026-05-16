package com.student.demo.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.demo.config.RabbitMQConfig;
import com.student.demo.entity.OutboxEvent;
import com.student.demo.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelayString = "${outbox.processor.delay:5000}")
    public void processOutboxEvents() {
        // Simple distributed lock to ensure only one API node sweeps at a time
        String lockKey = "lock:outbox:sweeper";
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 4, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(acquired)) {
            return;
        }

        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus("PENDING");
            if (!pendingEvents.isEmpty()) {
                logger.info("Found {} pending outbox events. Processing...", pendingEvents.size());
            }

            for (OutboxEvent event : pendingEvents) {
                try {
                    Map<String, Object> message = objectMapper.readValue(event.getPayload(), new TypeReference<Map<String, Object>>() {});
                    
                    rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME, 
                        RabbitMQConfig.ROUTING_KEY, 
                        message
                    );
                    
                    event.setStatus("PROCESSED");
                    outboxEventRepository.save(event);
                    logger.debug("Successfully published event ID: {}", event.getId());
                } catch (Exception e) {
                    logger.error("Failed to process outbox event ID: {}", event.getId(), e);
                    // Event remains in PENDING status, will be retried on next sweep
                }
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}

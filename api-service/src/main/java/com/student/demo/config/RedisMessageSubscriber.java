package com.student.demo.config;

import com.student.demo.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RedisMessageSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    @Autowired
    private SseService sseService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String eventData = new String(message.getBody());
        logger.info("Received event from Redis: {}", eventData);
        try {
            java.util.Map<String, Object> payload = objectMapper.readValue(eventData, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            Long fileId = ((Number) payload.get("fileId")).longValue();
            String status = (String) payload.get("status");
            sseService.dispatchEvent(fileId, status);
        } catch (Exception e) {
            logger.error("Failed to parse event from Redis: {}", eventData, e);
        }
    }
}

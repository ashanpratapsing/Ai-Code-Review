package com.student.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long fileId) {
        // Timeout of 30 minutes
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        emitters.put(fileId, emitter);

        emitter.onCompletion(() -> emitters.remove(fileId));
        emitter.onTimeout(() -> emitters.remove(fileId));
        emitter.onError((e) -> emitters.remove(fileId));

        return emitter;
    }

    public void dispatchEvent(Long fileId, String status) {
        SseEmitter emitter = emitters.get(fileId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("message").data(status));
                // Only keep it open if it's PENDING. Close it if it's done.
                if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    emitter.complete();
                    emitters.remove(fileId);
                }
            } catch (IOException e) {
                emitters.remove(fileId);
            }
        }
    }
}

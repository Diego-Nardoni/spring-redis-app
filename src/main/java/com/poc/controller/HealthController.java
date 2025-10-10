package com.poc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class HealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    public HealthController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test Redis connection
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            boolean redisHealthy = "PONG".equals(ping);
            
            response.put("status", redisHealthy ? "UP" : "DOWN");
            response.put("redis", redisHealthy ? "Connected" : "Disconnected");
            response.put("timestamp", System.currentTimeMillis());
            
            return redisHealthy ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(503).body(response);
                
        } catch (Exception e) {
            log.error("Health check failed", e);
            response.put("status", "DOWN");
            response.put("redis", "Error: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(response);
        }
    }
}

package com.poc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class HealthConfig {

    private final RedisTemplate<String, Object> redisTemplate;

    public HealthConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "spring-redis-poc");
        
        // Teste Redis opcional - não falha o health check se Redis estiver indisponível
        try {
            redisTemplate.opsForValue().set("health:ping", "pong");
            String result = (String) redisTemplate.opsForValue().get("health:ping");
            
            if ("pong".equals(result)) {
                response.put("redis", "Connected");
            } else {
                response.put("redis", "Connection test failed");
            }
        } catch (Exception e) {
            log.warn("Redis health check failed, but service is still UP: {}", e.getMessage());
            response.put("redis", "Unavailable - " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}

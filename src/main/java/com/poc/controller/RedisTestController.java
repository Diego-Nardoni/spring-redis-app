package com.poc.controller;

import com.poc.service.ServerlessCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/redis")
public class RedisTestController {

    private static final Logger log = LoggerFactory.getLogger(RedisTestController.class);
    private final ServerlessCacheService cacheService;

    public RedisTestController(ServerlessCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Testing Redis Serverless connection...");
            
            // Teste simples de set/get
            String testKey = "test:serverless:" + System.currentTimeMillis();
            String testValue = "Redis Serverless connection working!";
            
            cacheService.put(testKey, testValue, Duration.ofMinutes(5));
            Optional<String> retrievedValue = cacheService.get(testKey, String.class);
            
            response.put("status", "SUCCESS");
            response.put("message", "Redis Serverless connection working");
            response.put("testKey", testKey);
            response.put("testValue", testValue);
            response.put("retrievedValue", retrievedValue.orElse(null));
            response.put("match", retrievedValue.map(v -> v.equals(testValue)).orElse(false));
            response.put("cacheType", "serverless");
            
            log.info("Redis Serverless test successful: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Redis Serverless test failed", e);
            response.put("status", "ERROR");
            response.put("message", "Redis Serverless connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("cacheType", "serverless");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        Map<String, Object> response = new HashMap<>();
        
        boolean healthy = cacheService.isHealthy();
        response.put("status", healthy ? "UP" : "DOWN");
        response.put("type", "redis-serverless");
        response.put("healthy", healthy);
        
        return ResponseEntity.ok(response);
    }
}

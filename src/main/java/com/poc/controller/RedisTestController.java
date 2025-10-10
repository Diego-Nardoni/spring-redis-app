package com.poc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisTestController {

    private static final Logger log = LoggerFactory.getLogger(RedisTestController.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Testing Redis connection...");
            
            // Teste simples de set/get
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Redis TLS connection working!";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            response.put("status", "SUCCESS");
            response.put("message", "Redis connection working");
            response.put("testKey", testKey);
            response.put("testValue", testValue);
            response.put("retrievedValue", retrievedValue);
            response.put("match", testValue.equals(retrievedValue));
            
            log.info("Redis test successful: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Redis test failed", e);
            response.put("status", "ERROR");
            response.put("message", "Redis connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}

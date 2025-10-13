package com.poc.controller;

import com.poc.service.ServerlessCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    private final ServerlessCacheService cacheService;

    public HealthCheckController(ServerlessCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        boolean redisHealthy = cacheService.isHealthy();
        
        response.put("status", redisHealthy ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "spring-redis-poc");
        response.put("version", "1.0.0");
        response.put("redis", Map.of(
            "status", redisHealthy ? "UP" : "DOWN",
            "type", "serverless"
        ));
        
        return ResponseEntity.ok(response);
    }
}

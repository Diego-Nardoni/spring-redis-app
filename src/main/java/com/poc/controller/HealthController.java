package com.poc.controller;

import com.poc.service.ArchitectureTestService;
import com.poc.service.ServerlessCacheService;
import com.poc.model.ArchitectureStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private final ArchitectureTestService architectureTestService;
    private final ServerlessCacheService cacheService;

    public HealthController(ArchitectureTestService architectureTestService, 
                          ServerlessCacheService cacheService) {
        this.architectureTestService = architectureTestService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
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

    @GetMapping("/detailed")
    public ResponseEntity<ArchitectureStatus> detailedHealth() {
        try {
            ArchitectureStatus status = architectureTestService.getArchitectureStatus();
            
            if ("HEALTHY".equals(status.getOverallStatus())) {
                return ResponseEntity.ok(status);
            } else if ("DEGRADED".equals(status.getOverallStatus())) {
                return ResponseEntity.status(206).body(status); // 206 Partial Content
            } else {
                return ResponseEntity.status(503).body(status); // 503 Service Unavailable
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        Map<String, Object> response = new HashMap<>();
        boolean healthy = cacheService.isHealthy();
        
        response.put("status", healthy ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now());
        response.put("type", "redis-serverless");
        
        return ResponseEntity.ok(response);
    }
}

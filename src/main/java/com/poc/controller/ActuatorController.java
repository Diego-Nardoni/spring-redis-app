package com.poc.controller;

import com.poc.service.ServerlessCacheService;
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
@RequestMapping("/actuator")
public class ActuatorController {

    private static final Logger log = LoggerFactory.getLogger(ActuatorController.class);
    private final ServerlessCacheService cacheService;

    public ActuatorController(ServerlessCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Teste básico de conectividade Redis
            boolean redisHealthy = cacheService.isHealthy();
            
            response.put("status", redisHealthy ? "UP" : "DOWN");
            response.put("timestamp", LocalDateTime.now());
            response.put("checks", Map.of(
                "redis", Map.of("status", redisHealthy ? "UP" : "DOWN")
            ));
            
            if (redisHealthy) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            log.error("Readiness check failed", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

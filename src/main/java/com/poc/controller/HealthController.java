package com.poc.controller;

import com.poc.service.ArchitectureTestService;
import com.poc.model.ArchitectureStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private final ArchitectureTestService architectureTestService;

    public HealthController(ArchitectureTestService architectureTestService) {
        this.architectureTestService = architectureTestService;
    }

    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
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
}

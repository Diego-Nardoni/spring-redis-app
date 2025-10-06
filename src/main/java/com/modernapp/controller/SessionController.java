package com.modernapp.controller;

import com.modernapp.dto.SessionResponse;
import com.modernapp.service.RedisHealthService;
import com.modernapp.service.SessionService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;
    private final RedisHealthService redisHealthService;

    @GetMapping("/")
    public String dashboard(HttpServletRequest request, HttpSession session, Model model) {
        try {
            SessionResponse sessionResponse = sessionService.createOrUpdateSession(request, session);
            
            model.addAttribute("session", sessionResponse);
            model.addAttribute("redisInfo", redisHealthService.getRedisInfo());
            model.addAttribute("performanceTest", redisHealthService.testRedisPerformance());
            
            return "dashboard";
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load session data: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/api/session")
    @ResponseBody
    @Timed(value = "api.session.get", description = "Time taken to get session via API")
    public ResponseEntity<SessionResponse> getSession(HttpServletRequest request, HttpSession session) {
        try {
            SessionResponse response = sessionService.createOrUpdateSession(request, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting session via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/api/session/attribute")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setSessionAttribute(
            @RequestParam String key,
            @RequestParam String value,
            HttpSession session) {
        try {
            session.setAttribute(key, value);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "key", key,
                "value", value,
                "sessionId", session.getId()
            ));
        } catch (Exception e) {
            log.error("Error setting session attribute", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/api/session/analytics/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSessionAnalytics(@PathVariable String sessionId) {
        try {
            Map<String, Object> analytics = sessionService.getSessionAnalytics(sessionId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting session analytics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/health/redis")
    @ResponseBody
    public ResponseEntity<SessionResponse.HealthStatus> getRedisHealth() {
        try {
            SessionResponse.HealthStatus health = redisHealthService.getHealthStatus();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting Redis health", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/redis/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        try {
            Map<String, Object> info = redisHealthService.getRedisInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Error getting Redis info", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/redis/performance-test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> performanceTest() {
        try {
            boolean result = redisHealthService.testRedisPerformance();
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "Performance test passed" : "Performance test failed"
            ));
        } catch (Exception e) {
            log.error("Error running performance test", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

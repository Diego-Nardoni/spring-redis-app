package com.modernapp.service;

import com.modernapp.dto.SessionResponse;
import com.modernapp.model.SessionData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisHealthService redisHealthService;

    @Value("${app.container.id:unknown}")
    private String containerId;

    @Value("${app.region:us-east-1}")
    private String region;

    @Timed(value = "session.create", description = "Time taken to create session")
    @CircuitBreaker(name = "redis", fallbackMethod = "createSessionFallback")
    @Retry(name = "redis")
    public SessionResponse createOrUpdateSession(HttpServletRequest request, HttpSession session) {
        long startTime = System.currentTimeMillis();
        
        try {
            String sessionId = session.getId();
            String userId = Optional.ofNullable((String) session.getAttribute("userId"))
                    .orElse(UUID.randomUUID().toString());
            
            // Update request count
            Long requestCount = Optional.ofNullable((Long) session.getAttribute("requestCount"))
                    .orElse(0L);
            requestCount++;
            session.setAttribute("requestCount", requestCount);
            session.setAttribute("userId", userId);
            session.setAttribute("lastAccess", Instant.now().toString());

            // Create session data
            SessionData sessionData = SessionData.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .isNew(session.isNew())
                    .creationTime(Instant.ofEpochMilli(session.getCreationTime()))
                    .lastAccessedTime(Instant.ofEpochMilli(session.getLastAccessedTime()))
                    .maxInactiveInterval(session.getMaxInactiveInterval())
                    .attributes(getSessionAttributes(session))
                    .containerInfo(containerId)
                    .region(region)
                    .requestCount(requestCount)
                    .userAgent(request.getHeader("User-Agent"))
                    .clientIp(getClientIp(request))
                    .build();

            // Store in Redis for analytics
            storeSessionAnalytics(sessionData);

            long sessionTime = System.currentTimeMillis() - startTime;
            
            return SessionResponse.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .isNew(session.isNew())
                    .creationTime(sessionData.getCreationTime())
                    .lastAccessedTime(sessionData.getLastAccessedTime())
                    .requestCount(requestCount)
                    .containerInfo(containerId)
                    .region(region)
                    .customAttributes(sessionData.getAttributes())
                    .redisHealth(redisHealthService.getHealthStatus())
                    .performance(SessionResponse.PerformanceMetrics.builder()
                            .sessionRetrievalTimeMs(sessionTime)
                            .sessionSaveTimeMs(sessionTime)
                            .activeConnections(getActiveConnections())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error creating/updating session", e);
            throw e;
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "getSessionAnalyticsFallback")
    public Map<String, Object> getSessionAnalytics(String sessionId) {
        try {
            String key = "session:analytics:" + sessionId;
            return (Map<String, Object>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error retrieving session analytics", e);
            return new HashMap<>();
        }
    }

    private void storeSessionAnalytics(SessionData sessionData) {
        try {
            String key = "session:analytics:" + sessionData.getSessionId();
            redisTemplate.opsForValue().set(key, sessionData, Duration.ofHours(24));
            
            // Store user session mapping
            String userKey = "user:sessions:" + sessionData.getUserId();
            redisTemplate.opsForSet().add(userKey, sessionData.getSessionId());
            redisTemplate.expire(userKey, Duration.ofHours(24));
            
        } catch (Exception e) {
            log.warn("Failed to store session analytics", e);
        }
    }

    private Map<String, Object> getSessionAttributes(HttpSession session) {
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(name -> 
            attributes.put(name, session.getAttribute(name)));
        return attributes;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private int getActiveConnections() {
        try {
            return redisTemplate.getConnectionFactory().getConnection().info("clients")
                    .getProperty("connected_clients") != null ? 
                    Integer.parseInt(redisTemplate.getConnectionFactory().getConnection()
                            .info("clients").getProperty("connected_clients")) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Fallback methods
    public SessionResponse createSessionFallback(HttpServletRequest request, HttpSession session, Exception ex) {
        log.warn("Using fallback for session creation", ex);
        return SessionResponse.builder()
                .sessionId(session.getId())
                .userId("fallback-user")
                .isNew(session.isNew())
                .creationTime(Instant.ofEpochMilli(session.getCreationTime()))
                .lastAccessedTime(Instant.ofEpochMilli(session.getLastAccessedTime()))
                .requestCount(1L)
                .containerInfo(containerId)
                .region(region)
                .customAttributes(new HashMap<>())
                .redisHealth(SessionResponse.HealthStatus.builder()
                        .connected(false)
                        .status("CIRCUIT_OPEN")
                        .responseTimeMs(-1)
                        .build())
                .build();
    }

    public Map<String, Object> getSessionAnalyticsFallback(String sessionId, Exception ex) {
        log.warn("Using fallback for session analytics", ex);
        return Map.of("error", "Redis unavailable", "sessionId", sessionId);
    }
}

package com.modernapp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class SessionResponse {
    private String sessionId;
    private String userId;
    private boolean isNew;
    private Instant creationTime;
    private Instant lastAccessedTime;
    private long requestCount;
    private String containerInfo;
    private String region;
    private Map<String, Object> customAttributes;
    private HealthStatus redisHealth;
    private PerformanceMetrics performance;

    @Data
    @Builder
    @Jacksonized
    public static class HealthStatus {
        private boolean connected;
        private long responseTimeMs;
        private String status;
    }

    @Data
    @Builder
    @Jacksonized
    public static class PerformanceMetrics {
        private long sessionRetrievalTimeMs;
        private long sessionSaveTimeMs;
        private int activeConnections;
    }
}

package com.modernapp.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class SessionData {
    private String sessionId;
    private String userId;
    private boolean isNew;
    private Instant creationTime;
    private Instant lastAccessedTime;
    private int maxInactiveInterval;
    private Map<String, Object> attributes;
    private String containerInfo;
    private String region;
    private long requestCount;
    private String userAgent;
    private String clientIp;
}

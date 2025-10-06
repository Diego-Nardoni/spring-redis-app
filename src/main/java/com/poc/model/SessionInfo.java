package com.poc.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class SessionInfo {
    private String sessionId;
    private boolean isNew;
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;
    private Map<String, Object> attributes;
    private Integer counter;
}

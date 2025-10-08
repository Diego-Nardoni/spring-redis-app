package com.poc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionInfo {
    private String sessionId;
    private Integer counter;
    private boolean isNew;
    private long creationTime;
    private long lastAccessedTime;
    private String containerInfo;
    
    public String getFormattedCreationTime() {
        return java.time.Instant.ofEpochMilli(creationTime).toString();
    }
    
    public String getFormattedLastAccessTime() {
        return java.time.Instant.ofEpochMilli(lastAccessedTime).toString();
    }
}

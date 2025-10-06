package com.poc.model;

import java.time.Instant;
import java.util.Map;

public class SessionInfo {
    private String sessionId;
    private boolean isNew;
    private Instant creationTime;
    private Instant lastAccessedTime;
    private int maxInactiveInterval;
    private Map<String, Object> attributes;
    
    // Constructors
    public SessionInfo() {}
    
    public SessionInfo(String sessionId, boolean isNew, Instant creationTime, 
                      Instant lastAccessedTime, int maxInactiveInterval, 
                      Map<String, Object> attributes) {
        this.sessionId = sessionId;
        this.isNew = isNew;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.attributes = attributes;
    }
    
    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public boolean isNew() { return isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    
    public Instant getCreationTime() { return creationTime; }
    public void setCreationTime(Instant creationTime) { this.creationTime = creationTime; }
    
    public Instant getLastAccessedTime() { return lastAccessedTime; }
    public void setLastAccessedTime(Instant lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }
    
    public int getMaxInactiveInterval() { return maxInactiveInterval; }
    public void setMaxInactiveInterval(int maxInactiveInterval) { this.maxInactiveInterval = maxInactiveInterval; }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}

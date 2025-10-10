package com.poc.model;

public class SessionInfo {
    private String sessionId;
    private String userId;
    private long createdAt;
    private long lastAccessed;

    public SessionInfo() {}

    public SessionInfo(String sessionId, String userId, long createdAt, long lastAccessed) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.lastAccessed = lastAccessed;
    }

    public static SessionInfoBuilder builder() {
        return new SessionInfoBuilder();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(long lastAccessed) { this.lastAccessed = lastAccessed; }

    public static class SessionInfoBuilder {
        private String sessionId;
        private String userId;
        private long createdAt;
        private long lastAccessed;

        public SessionInfoBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public SessionInfoBuilder userId(String userId) { this.userId = userId; return this; }
        public SessionInfoBuilder createdAt(long createdAt) { this.createdAt = createdAt; return this; }
        public SessionInfoBuilder lastAccessed(long lastAccessed) { this.lastAccessed = lastAccessed; return this; }
        
        public SessionInfo build() {
            return new SessionInfo(sessionId, userId, createdAt, lastAccessed);
        }
    }
}

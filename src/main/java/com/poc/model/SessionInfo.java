package com.poc.model;

public class SessionInfo {
    private String sessionId;
    private String userId;
    private long createdAt;
    private long lastAccessed;
    private Integer counter;
    private boolean isNew;
    private String containerInfo;

    public SessionInfo() {}

    public SessionInfo(String sessionId, String userId, long createdAt, long lastAccessed) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.lastAccessed = lastAccessed;
        this.counter = 0;
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
    
    public Integer getCounter() { return counter; }
    public void setCounter(Integer counter) { this.counter = counter; }
    
    public boolean isNew() { return isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    
    public String getContainerInfo() { return containerInfo; }
    public void setContainerInfo(String containerInfo) { this.containerInfo = containerInfo; }

    public static class SessionInfoBuilder {
        private String sessionId;
        private String userId;
        private long createdAt;
        private long lastAccessed;
        private Integer counter;
        private boolean isNew;
        private String containerInfo;

        public SessionInfoBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public SessionInfoBuilder userId(String userId) { this.userId = userId; return this; }
        public SessionInfoBuilder createdAt(long createdAt) { this.createdAt = createdAt; return this; }
        public SessionInfoBuilder lastAccessed(long lastAccessed) { this.lastAccessed = lastAccessed; return this; }
        public SessionInfoBuilder counter(Integer counter) { this.counter = counter; return this; }
        public SessionInfoBuilder isNew(boolean isNew) { this.isNew = isNew; return this; }
        public SessionInfoBuilder creationTime(long creationTime) { this.createdAt = creationTime; return this; }
        public SessionInfoBuilder lastAccessedTime(long lastAccessedTime) { this.lastAccessed = lastAccessedTime; return this; }
        public SessionInfoBuilder containerInfo(String containerInfo) { this.containerInfo = containerInfo; return this; }
        
        public SessionInfo build() {
            SessionInfo si = new SessionInfo(sessionId, userId, createdAt, lastAccessed);
            si.setCounter(counter);
            si.setNew(isNew);
            si.setContainerInfo(containerInfo);
            return si;
        }
    }
}

package com.poc.model;

public class SessionInfo {
    private String sessionId;
    private String containerId;
    private String timestamp;
    private int visitCount;

    public SessionInfo() {}

    public static SessionInfoBuilder builder() {
        return new SessionInfoBuilder();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getVisitCount() { return visitCount; }
    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }

    public static class SessionInfoBuilder {
        private SessionInfo info = new SessionInfo();

        public SessionInfoBuilder sessionId(String sessionId) { info.sessionId = sessionId; return this; }
        public SessionInfoBuilder containerId(String containerId) { info.containerId = containerId; return this; }
        public SessionInfoBuilder timestamp(String timestamp) { info.timestamp = timestamp; return this; }
        public SessionInfoBuilder visitCount(int visitCount) { info.visitCount = visitCount; return this; }
        public SessionInfoBuilder counter(Integer counter) { info.visitCount = counter; return this; }
        public SessionInfoBuilder isNew(boolean isNew) { return this; }
        public SessionInfoBuilder creationTime(long creationTime) { return this; }
        public SessionInfoBuilder lastAccessedTime(long lastAccessedTime) { return this; }
        public SessionInfoBuilder containerInfo(String containerInfo) { return this; }

        public SessionInfo build() { return info; }
    }
}

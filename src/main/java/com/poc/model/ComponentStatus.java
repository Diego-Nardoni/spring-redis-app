package com.poc.model;

public class ComponentStatus {
    private String name;
    private String status;
    private String message;
    private long responseTime;

    public ComponentStatus() {}

    public ComponentStatus(String name, String status, String message, long responseTime) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.responseTime = responseTime;
    }

    public static ComponentStatusBuilder builder() {
        return new ComponentStatusBuilder();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }

    public static class ComponentStatusBuilder {
        private String name;
        private String status;
        private String message;
        private long responseTime;

        public ComponentStatusBuilder name(String name) { this.name = name; return this; }
        public ComponentStatusBuilder status(String status) { this.status = status; return this; }
        public ComponentStatusBuilder message(String message) { this.message = message; return this; }
        public ComponentStatusBuilder details(String details) { this.message = details; return this; }
        public ComponentStatusBuilder responseTime(long responseTime) { this.responseTime = responseTime; return this; }

        public ComponentStatus build() {
            return new ComponentStatus(name, status, message, responseTime);
        }
    }
}

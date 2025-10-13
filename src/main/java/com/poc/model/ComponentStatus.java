package com.poc.model;

public class ComponentStatus {
    private String name;
    private String status;
    private String message;
    private String details;
    private long responseTime;

    public ComponentStatus() {}

    public ComponentStatus(String name, String status, String message, String details) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.details = details;
        this.responseTime = 0;
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
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    
    public String getStatusColor() {
        switch (status) {
            case "healthy": return "success";
            case "warning": return "warning";
            case "error": return "danger";
            default: return "secondary";
        }
    }
    
    public String getStatusIcon() {
        switch (status) {
            case "healthy": return "✅";
            case "warning": return "⚠️";
            case "error": return "❌";
            default: return "❓";
        }
    }

    public static class ComponentStatusBuilder {
        private String name;
        private String status;
        private String message;
        private String details;
        private long responseTime;

        public ComponentStatusBuilder name(String name) { this.name = name; return this; }
        public ComponentStatusBuilder status(String status) { this.status = status; return this; }
        public ComponentStatusBuilder message(String message) { this.message = message; return this; }
        public ComponentStatusBuilder details(String details) { this.details = details; return this; }
        public ComponentStatusBuilder responseTime(long responseTime) { this.responseTime = responseTime; return this; }
        
        public ComponentStatus build() {
            ComponentStatus cs = new ComponentStatus(name, status, message, details);
            cs.setResponseTime(responseTime);
            return cs;
        }
    }
}

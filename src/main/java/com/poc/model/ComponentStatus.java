package com.poc.model;

public class ComponentStatus {
    private String name;
    private String status;
    private String message;
    private String details;

    public ComponentStatus() {}

    public ComponentStatus(String name, String status, String message, String details) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.details = details;
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

    public static class ComponentStatusBuilder {
        private String name;
        private String status;
        private String message;
        private String details;

        public ComponentStatusBuilder name(String name) { this.name = name; return this; }
        public ComponentStatusBuilder status(String status) { this.status = status; return this; }
        public ComponentStatusBuilder message(String message) { this.message = message; return this; }
        public ComponentStatusBuilder details(String details) { this.details = details; return this; }
        
        public ComponentStatus build() {
            return new ComponentStatus(name, status, message, details);
        }
    }
}

package com.poc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentStatus {
    private String name;
    private String status; // healthy, warning, error, unknown
    private String details;
    private Long responseTime; // in milliseconds
    
    public String getStatusColor() {
        switch (status.toLowerCase()) {
            case "healthy": return "success";
            case "warning": return "warning";
            case "error": return "danger";
            default: return "secondary";
        }
    }
    
    public String getStatusIcon() {
        switch (status.toLowerCase()) {
            case "healthy": return "✅";
            case "warning": return "⚠️";
            case "error": return "❌";
            default: return "❓";
        }
    }
}

package com.poc.model;

import java.util.List;

public class ArchitectureStatus {
    private String overallStatus;
    private List<ComponentStatus> components;

    public ArchitectureStatus() {}

    public ArchitectureStatus(String overallStatus, List<ComponentStatus> components) {
        this.overallStatus = overallStatus;
        this.components = components;
    }

    public String calculateOverallStatus(ComponentStatus containerStatus, ComponentStatus redisStatus, 
                                       ComponentStatus sessionStatus, ComponentStatus cloudFrontStatus) {
        if ("HEALTHY".equals(containerStatus.getStatus()) && 
            "HEALTHY".equals(redisStatus.getStatus()) && 
            "HEALTHY".equals(sessionStatus.getStatus()) && 
            "HEALTHY".equals(cloudFrontStatus.getStatus())) {
            return "HEALTHY";
        } else if ("UNHEALTHY".equals(containerStatus.getStatus()) || 
                   "UNHEALTHY".equals(redisStatus.getStatus()) || 
                   "UNHEALTHY".equals(sessionStatus.getStatus()) || 
                   "UNHEALTHY".equals(cloudFrontStatus.getStatus())) {
            return "UNHEALTHY";
        } else {
            return "DEGRADED";
        }
    }

    public static ArchitectureStatusBuilder builder() {
        return new ArchitectureStatusBuilder();
    }

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    
    public List<ComponentStatus> getComponents() { return components; }
    public void setComponents(List<ComponentStatus> components) { this.components = components; }

    public static class ArchitectureStatusBuilder {
        private String overallStatus;
        private List<ComponentStatus> components;
        private ComponentStatus containerStatus;

        public ArchitectureStatusBuilder overallStatus(String overallStatus) { 
            this.overallStatus = overallStatus; 
            return this; 
        }
        
        public ArchitectureStatusBuilder components(List<ComponentStatus> components) { 
            this.components = components; 
            return this; 
        }
        
        public ArchitectureStatusBuilder containerStatus(ComponentStatus containerStatus) { 
            this.containerStatus = containerStatus; 
            return this; 
        }
        
        public ArchitectureStatusBuilder redisStatus(ComponentStatus redisStatus) { 
            return this; 
        }
        
        public ArchitectureStatusBuilder sessionStatus(ComponentStatus sessionStatus) { 
            return this; 
        }
        
        public ArchitectureStatusBuilder cloudFrontStatus(ComponentStatus cloudFrontStatus) { 
            return this; 
        }
        
        public ArchitectureStatusBuilder timestamp(String timestamp) { 
            return this; 
        }
        
        public ArchitectureStatus build() {
            return new ArchitectureStatus(overallStatus, components);
        }
    }
}

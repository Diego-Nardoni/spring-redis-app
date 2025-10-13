package com.poc.model;

import java.util.List;

public class ArchitectureStatus {
    private String overallStatus;
    private List<ComponentStatus> components;
    private int healthyCount;
    private boolean allHealthy;
    private ComponentStatus containerStatus;
    private ComponentStatus redisStatus;
    private ComponentStatus sessionStatus;
    private ComponentStatus cloudFrontStatus;

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
    
    public int getHealthyCount() { return healthyCount; }
    public void setHealthyCount(int healthyCount) { this.healthyCount = healthyCount; }
    
    public boolean isAllHealthy() { return allHealthy; }
    public void setAllHealthy(boolean allHealthy) { this.allHealthy = allHealthy; }
    
    public ComponentStatus getContainerStatus() { return containerStatus; }
    public void setContainerStatus(ComponentStatus containerStatus) { this.containerStatus = containerStatus; }
    
    public ComponentStatus getRedisStatus() { return redisStatus; }
    public void setRedisStatus(ComponentStatus redisStatus) { this.redisStatus = redisStatus; }
    
    public ComponentStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(ComponentStatus sessionStatus) { this.sessionStatus = sessionStatus; }
    
    public ComponentStatus getCloudFrontStatus() { return cloudFrontStatus; }
    public void setCloudFrontStatus(ComponentStatus cloudFrontStatus) { this.cloudFrontStatus = cloudFrontStatus; }

    public static class ArchitectureStatusBuilder {
        private String overallStatus;
        private List<ComponentStatus> components;
        private ComponentStatus containerStatus;
        private ComponentStatus redisStatus;
        private ComponentStatus sessionStatus;
        private ComponentStatus cloudFrontStatus;

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
            this.redisStatus = redisStatus;
            return this; 
        }
        
        public ArchitectureStatusBuilder sessionStatus(ComponentStatus sessionStatus) { 
            this.sessionStatus = sessionStatus;
            return this; 
        }
        
        public ArchitectureStatusBuilder cloudFrontStatus(ComponentStatus cloudFrontStatus) { 
            this.cloudFrontStatus = cloudFrontStatus;
            return this; 
        }
        
        public ArchitectureStatusBuilder timestamp(String timestamp) { 
            return this; 
        }
        
        public ArchitectureStatus build() {
            // Build components list
            List<ComponentStatus> componentsList = java.util.Arrays.asList(
                containerStatus, redisStatus, sessionStatus, cloudFrontStatus
            );
            
            // Calculate healthy count
            int healthyCount = (int) componentsList.stream()
                .filter(c -> "healthy".equals(c.getStatus()))
                .count();
            
            // Calculate overall status if not set
            String finalOverallStatus = overallStatus;
            if (finalOverallStatus == null) {
                finalOverallStatus = calculateOverallStatus(containerStatus, redisStatus, sessionStatus, cloudFrontStatus);
            }
            
            ArchitectureStatus status = new ArchitectureStatus(finalOverallStatus, componentsList);
            status.setHealthyCount(healthyCount);
            status.setAllHealthy(healthyCount == 4);
            status.setContainerStatus(containerStatus);
            status.setRedisStatus(redisStatus);
            status.setSessionStatus(sessionStatus);
            status.setCloudFrontStatus(cloudFrontStatus);
            return status;
        }
        
        private String calculateOverallStatus(ComponentStatus containerStatus, ComponentStatus redisStatus, 
                                           ComponentStatus sessionStatus, ComponentStatus cloudFrontStatus) {
            if ("healthy".equals(containerStatus.getStatus()) && 
                "healthy".equals(redisStatus.getStatus()) && 
                "healthy".equals(sessionStatus.getStatus()) && 
                "healthy".equals(cloudFrontStatus.getStatus())) {
                return "HEALTHY";
            } else if ("error".equals(containerStatus.getStatus()) || 
                       "error".equals(redisStatus.getStatus()) || 
                       "error".equals(sessionStatus.getStatus()) || 
                       "error".equals(cloudFrontStatus.getStatus())) {
                return "UNHEALTHY";
            } else {
                return "DEGRADED";
            }
        }
    }
}

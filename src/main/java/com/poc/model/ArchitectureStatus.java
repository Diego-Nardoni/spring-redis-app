package com.poc.model;

public class ArchitectureStatus {
    private String overallStatus;
    private ComponentStatus containerStatus;
    private ComponentStatus redisStatus;
    private ComponentStatus sessionStatus;
    private ComponentStatus cloudFrontStatus;

    public ArchitectureStatus() {}

    public ArchitectureStatus(String overallStatus, ComponentStatus containerStatus, 
                            ComponentStatus redisStatus, ComponentStatus sessionStatus, 
                            ComponentStatus cloudFrontStatus) {
        this.overallStatus = overallStatus;
        this.containerStatus = containerStatus;
        this.redisStatus = redisStatus;
        this.sessionStatus = sessionStatus;
        this.cloudFrontStatus = cloudFrontStatus;
    }

    public String getOverallStatus() {
        if (containerStatus.getStatus().equals("UP") && 
            redisStatus.getStatus().equals("UP") && 
            sessionStatus.getStatus().equals("UP") && 
            cloudFrontStatus.getStatus().equals("UP")) {
            return "HEALTHY";
        } else if (containerStatus.getStatus().equals("DOWN") || 
                   redisStatus.getStatus().equals("DOWN") || 
                   sessionStatus.getStatus().equals("DOWN") || 
                   cloudFrontStatus.getStatus().equals("DOWN")) {
            return "UNHEALTHY";
        }
        return "DEGRADED";
    }

    public static ArchitectureStatusBuilder builder() {
        return new ArchitectureStatusBuilder();
    }

    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
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
        private ComponentStatus containerStatus;
        private ComponentStatus redisStatus;
        private ComponentStatus sessionStatus;
        private ComponentStatus cloudFrontStatus;

        public ArchitectureStatusBuilder overallStatus(String overallStatus) { this.overallStatus = overallStatus; return this; }
        public ArchitectureStatusBuilder timestamp(String timestamp) { return this; }
        public ArchitectureStatusBuilder containerStatus(ComponentStatus containerStatus) { this.containerStatus = containerStatus; return this; }
        public ArchitectureStatusBuilder redisStatus(ComponentStatus redisStatus) { this.redisStatus = redisStatus; return this; }
        public ArchitectureStatusBuilder sessionStatus(ComponentStatus sessionStatus) { this.sessionStatus = sessionStatus; return this; }
        public ArchitectureStatusBuilder cloudFrontStatus(ComponentStatus cloudFrontStatus) { this.cloudFrontStatus = cloudFrontStatus; return this; }

        public ArchitectureStatus build() {
            return new ArchitectureStatus(overallStatus, containerStatus, redisStatus, sessionStatus, cloudFrontStatus);
        }
    }
}

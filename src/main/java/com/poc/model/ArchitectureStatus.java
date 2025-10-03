package com.poc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArchitectureStatus {
    private ComponentStatus containerStatus;
    private ComponentStatus redisStatus;
    private ComponentStatus sessionStatus;
    private ComponentStatus cloudFrontStatus;
    private String timestamp;
    
    public boolean isAllHealthy() {
        return "healthy".equals(containerStatus.getStatus()) &&
               "healthy".equals(redisStatus.getStatus()) &&
               "healthy".equals(sessionStatus.getStatus()) &&
               "healthy".equals(cloudFrontStatus.getStatus());
    }
    
    public int getHealthyCount() {
        int count = 0;
        if ("healthy".equals(containerStatus.getStatus())) count++;
        if ("healthy".equals(redisStatus.getStatus())) count++;
        if ("healthy".equals(sessionStatus.getStatus())) count++;
        if ("healthy".equals(cloudFrontStatus.getStatus())) count++;
        return count;
    }
}

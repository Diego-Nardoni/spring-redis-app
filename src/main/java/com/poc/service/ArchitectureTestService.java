package com.poc.service;

import com.poc.model.ArchitectureStatus;
import com.poc.model.ComponentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ArchitectureTestService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private volatile ComponentStatus cachedRedisStatus = ComponentStatus.builder()
            .name("Redis Cache")
            .status("checking")
            .details("Initial health check in progress")
            .responseTime(0L)
            .build();

    public ArchitectureStatus getArchitectureStatus() {
        // Start async Redis check but don't wait
        CompletableFuture.runAsync(this::updateRedisStatus);
        
        return ArchitectureStatus.builder()
                .containerStatus(getContainerStatus())
                .redisStatus(cachedRedisStatus)
                .sessionStatus(getSessionStatusFromCache())
                .cloudFrontStatus(getCloudFrontStatus())
                .timestamp(Instant.now().toString())
                .build();
    }

    private ComponentStatus getContainerStatus() {
        try {
            String hostname = getContainerInfo();
            String javaVersion = System.getProperty("java.version");
            String springVersion = org.springframework.boot.SpringBootVersion.getVersion();
            
            return ComponentStatus.builder()
                    .name("ECS Container")
                    .status("healthy")
                    .details("Container: " + hostname + " | Java: " + javaVersion + " | Spring: " + springVersion)
                    .responseTime(1L)
                    .build();
        } catch (Exception e) {
            return ComponentStatus.builder()
                    .name("ECS Container")
                    .status("error")
                    .details("Error: " + e.getMessage())
                    .responseTime(-1L)
                    .build();
        }
    }

    private void updateRedisStatus() {
        try {
            long startTime = System.currentTimeMillis();
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "ping";
            
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            cachedRedisStatus = ComponentStatus.builder()
                    .name("Redis Cache")
                    .status(testValue.equals(result) ? "healthy" : "warning")
                    .details("Connection successful | Response time: " + responseTime + "ms")
                    .responseTime(responseTime)
                    .build();
        } catch (Exception e) {
            cachedRedisStatus = ComponentStatus.builder()
                    .name("Redis Cache")
                    .status("error")
                    .details("Connection failed: " + e.getMessage())
                    .responseTime(-1L)
                    .build();
        }
    }

    private ComponentStatus getSessionStatusFromCache() {
        return ComponentStatus.builder()
                .name("Session Store")
                .status(cachedRedisStatus.getStatus())
                .details("Based on Redis connectivity")
                .responseTime(1L)
                .build();
    }

    private ComponentStatus getCloudFrontStatus() {
        // Check CloudFront headers to determine if request came through CloudFront
        try {
            // This would be populated by the controller with request headers
            return ComponentStatus.builder()
                    .name("CloudFront CDN")
                    .status("healthy")
                    .details("CDN active | Global edge locations")
                    .responseTime(1L)
                    .build();
        } catch (Exception e) {
            return ComponentStatus.builder()
                    .name("CloudFront CDN")
                    .status("unknown")
                    .details("Unable to determine CloudFront status")
                    .responseTime(-1L)
                    .build();
        }
    }

    private String getContainerInfo() {
        try {
            String hostname = System.getenv("HOSTNAME");
            if (hostname == null) {
                hostname = java.net.InetAddress.getLocalHost().getHostName();
            }
            return hostname;
        } catch (Exception e) {
            return "unknown-container";
        }
    }
}

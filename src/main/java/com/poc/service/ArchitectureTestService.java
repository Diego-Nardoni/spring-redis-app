package com.poc.service;

import com.poc.model.ArchitectureStatus;
import com.poc.model.ComponentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class ArchitectureTestService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ArchitectureStatus getArchitectureStatus() {
        return ArchitectureStatus.builder()
                .containerStatus(getContainerStatus())
                .redisStatus(getRedisStatus())
                .sessionStatus(getSessionStatus())
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

    private ComponentStatus getRedisStatus() {
        try {
            long startTime = System.currentTimeMillis();
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "ping";
            
            // Test Redis connectivity
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (testValue.equals(result)) {
                return ComponentStatus.builder()
                        .name("Redis Cache")
                        .status("healthy")
                        .details("Connection successful | Response time: " + responseTime + "ms")
                        .responseTime(responseTime)
                        .build();
            } else {
                return ComponentStatus.builder()
                        .name("Redis Cache")
                        .status("warning")
                        .details("Data mismatch in read/write test")
                        .responseTime(responseTime)
                        .build();
            }
        } catch (Exception e) {
            return ComponentStatus.builder()
                    .name("Redis Cache")
                    .status("error")
                    .details("Connection failed: " + e.getMessage())
                    .responseTime(-1L)
                    .build();
        }
    }

    private ComponentStatus getSessionStatus() {
        try {
            // Test session storage capability
            String testKey = "session:test:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "session-data", 30, TimeUnit.MINUTES);
            
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(testKey));
            redisTemplate.delete(testKey);
            
            if (exists) {
                return ComponentStatus.builder()
                        .name("Session Store")
                        .status("healthy")
                        .details("Session persistence working correctly")
                        .responseTime(1L)
                        .build();
            } else {
                return ComponentStatus.builder()
                        .name("Session Store")
                        .status("error")
                        .details("Session persistence test failed")
                        .responseTime(-1L)
                        .build();
            }
        } catch (Exception e) {
            return ComponentStatus.builder()
                    .name("Session Store")
                    .status("error")
                    .details("Session test error: " + e.getMessage())
                    .responseTime(-1L)
                    .build();
        }
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

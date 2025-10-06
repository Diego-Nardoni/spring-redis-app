package com.modernapp.service;

import com.modernapp.dto.SessionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisHealthService {

    private final RedisTemplate<String, Object> redisTemplate;

    @CircuitBreaker(name = "redis", fallbackMethod = "getHealthStatusFallback")
    public SessionResponse.HealthStatus getHealthStatus() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Test Redis connectivity with ping
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            long responseTime = System.currentTimeMillis() - startTime;
            
            boolean isHealthy = "PONG".equals(pong);
            
            return SessionResponse.HealthStatus.builder()
                    .connected(isHealthy)
                    .responseTimeMs(responseTime)
                    .status(isHealthy ? "HEALTHY" : "UNHEALTHY")
                    .build();
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Redis health check failed", e);
            
            return SessionResponse.HealthStatus.builder()
                    .connected(false)
                    .responseTimeMs(responseTime)
                    .status("ERROR: " + e.getMessage())
                    .build();
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "getRedisInfoFallback")
    public Map<String, Object> getRedisInfo() {
        try {
            var connection = redisTemplate.getConnectionFactory().getConnection();
            var info = connection.info();
            
            return Map.of(
                "version", info.getProperty("redis_version", "unknown"),
                "uptime", info.getProperty("uptime_in_seconds", "0"),
                "connected_clients", info.getProperty("connected_clients", "0"),
                "used_memory", info.getProperty("used_memory_human", "0"),
                "total_commands_processed", info.getProperty("total_commands_processed", "0"),
                "keyspace_hits", info.getProperty("keyspace_hits", "0"),
                "keyspace_misses", info.getProperty("keyspace_misses", "0")
            );
        } catch (Exception e) {
            log.error("Failed to get Redis info", e);
            return Map.of("error", e.getMessage());
        }
    }

    public boolean testRedisPerformance() {
        try {
            String testKey = "performance:test:" + System.currentTimeMillis();
            String testValue = "performance-test-value";
            
            // Test write performance
            long writeStart = System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofMinutes(1));
            long writeTime = System.currentTimeMillis() - writeStart;
            
            // Test read performance
            long readStart = System.currentTimeMillis();
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            long readTime = System.currentTimeMillis() - readStart;
            
            // Cleanup
            redisTemplate.delete(testKey);
            
            log.info("Redis performance test - Write: {}ms, Read: {}ms", writeTime, readTime);
            
            return testValue.equals(retrievedValue) && writeTime < 100 && readTime < 50;
            
        } catch (Exception e) {
            log.error("Redis performance test failed", e);
            return false;
        }
    }

    // Fallback methods
    public SessionResponse.HealthStatus getHealthStatusFallback(Exception ex) {
        return SessionResponse.HealthStatus.builder()
                .connected(false)
                .responseTimeMs(-1)
                .status("CIRCUIT_OPEN")
                .build();
    }

    public Map<String, Object> getRedisInfoFallback(Exception ex) {
        return Map.of(
            "error", "Circuit breaker open",
            "status", "unavailable"
        );
    }
}

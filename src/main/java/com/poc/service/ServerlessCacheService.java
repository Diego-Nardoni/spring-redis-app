package com.poc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ServerlessCacheService {

    private static final Logger log = LoggerFactory.getLogger(ServerlessCacheService.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisServerlessMonitoringService monitoringService;

    public ServerlessCacheService(RedisTemplate<String, Object> redisTemplate,
                                RedisServerlessMonitoringService monitoringService) {
        this.redisTemplate = redisTemplate;
        this.monitoringService = monitoringService;
    }

    public void put(String key, Object value) {
        put(key, value, DEFAULT_TTL);
    }

    public void put(String key, Object value, Duration ttl) {
        monitoringService.executeWithMonitoring("put", () -> {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
            return null;
        });
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return monitoringService.executeWithMonitoring("get", () -> {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of(type.cast(value));
            }
            log.debug("Cache miss for key: {}", key);
            return Optional.<T>empty();
        });
    }

    public boolean exists(String key) {
        return monitoringService.executeWithMonitoring("exists", () -> {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        });
    }

    public void delete(String key) {
        monitoringService.executeWithMonitoring("delete", () -> {
            redisTemplate.delete(key);
            log.debug("Deleted key: {}", key);
            return null;
        });
    }

    public void expire(String key, Duration ttl) {
        monitoringService.executeWithMonitoring("expire", () -> {
            redisTemplate.expire(key, ttl);
            log.debug("Set expiration for key: {} to {}", key, ttl);
            return null;
        });
    }

    public boolean isHealthy() {
        return monitoringService.testConnection();
    }
}

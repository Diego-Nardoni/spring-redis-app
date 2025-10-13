package com.poc.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServerlessMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(RedisServerlessMonitoringService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Counter connectionSuccessCounter;
    private final Counter connectionFailureCounter;
    private final Timer operationTimer;
    private final MeterRegistry meterRegistry;

    public RedisServerlessMonitoringService(RedisTemplate<String, Object> redisTemplate, 
                                          MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        this.connectionSuccessCounter = Counter.builder("redis.connection.success")
                .description("Successful Redis connections")
                .register(meterRegistry);
        this.connectionFailureCounter = Counter.builder("redis.connection.failure")
                .description("Failed Redis connections")
                .register(meterRegistry);
        this.operationTimer = Timer.builder("redis.operation.duration")
                .description("Redis operation duration")
                .register(meterRegistry);
    }

    public boolean testConnection() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String result = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            
            connectionSuccessCounter.increment();
            log.debug("Redis ping successful: {}", result);
            return true;
        } catch (Exception e) {
            connectionFailureCounter.increment();
            log.warn("Redis connection test failed", e);
            return false;
        } finally {
            sample.stop(operationTimer);
        }
    }

    public <T> T executeWithMonitoring(String operation, RedisOperation<T> redisOperation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = redisOperation.execute();
            log.debug("Redis operation '{}' completed successfully", operation);
            return result;
        } catch (Exception e) {
            log.error("Redis operation '{}' failed", operation, e);
            throw e;
        } finally {
            sample.stop(Timer.builder("redis.operation")
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }

    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute();
    }
}

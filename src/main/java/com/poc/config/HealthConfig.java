package com.poc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class HealthConfig {

    private static final Logger log = LoggerFactory.getLogger(HealthConfig.class);

    @Bean
    public HealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return () -> {
            try {
                // Test Redis connection
                redisTemplate.getConnectionFactory().getConnection().ping();
                return Health.up()
                    .withDetail("redis", "Connection successful")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("redis", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }

    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> {
            try {
                // Add custom health checks here
                return Health.up()
                    .withDetail("application", "Running")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
            } catch (Exception e) {
                log.error("Custom health check failed", e);
                return Health.down()
                    .withDetail("application", "Error")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }
}

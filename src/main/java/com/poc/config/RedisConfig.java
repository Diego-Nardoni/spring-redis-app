package com.poc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${/poc/redis/endpoint}")
    private String redisHost;

    @Value("${/poc/redis/port}")
    private int redisPort;

    @Value("${/poc/redis/ssl:false}")
    private boolean useSsl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            log.info("Configuring Redis connection to {}:{} (SSL: {})", redisHost, redisPort, useSsl);
            
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisHost);
            config.setPort(redisPort);
            
            JedisConnectionFactory factory = new JedisConnectionFactory(config);
            factory.setUseSsl(useSsl);
            
            return factory;
        } catch (Exception e) {
            log.error("Failed to configure Redis connection", e);
            throw e;
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        
        try {
            // Test connection
            template.getConnectionFactory().getConnection().ping();
            log.info("Redis connection test successful");
        } catch (Exception e) {
            log.error("Redis connection test failed", e);
        }
        
        return template;
    }
}

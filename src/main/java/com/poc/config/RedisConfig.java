package com.poc.config;

import com.poc.service.ParameterStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Autowired
    private ParameterStoreService parameterStoreService;

    // Fallback para variáveis de ambiente ou valores padrão
    @Value("${SPRING_REDIS_HOST:poc-redis-01ndkd.serverless.use1.cache.amazonaws.com}")
    private String fallbackRedisHost;

    @Value("${SPRING_REDIS_PORT:6379}")
    private int fallbackRedisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Tentar buscar do Parameter Store primeiro
        String redisHost = parameterStoreService.getParameter("/poc/redis/endpoint", fallbackRedisHost);
        String redisPortStr = parameterStoreService.getParameter("/poc/redis/port", String.valueOf(fallbackRedisPort));
        
        int redisPort;
        try {
            redisPort = Integer.parseInt(redisPortStr);
        } catch (NumberFormatException e) {
            redisPort = fallbackRedisPort;
        }
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}

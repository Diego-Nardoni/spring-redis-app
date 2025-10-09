package com.poc.config;

import com.poc.service.ParameterStoreService;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Autowired
    private ParameterStoreService parameterStoreService;

    @Value("${SPRING_REDIS_HOST:poc-redis-01ndkd.serverless.use1.cache.amazonaws.com}")
    private String fallbackRedisHost;

    @Value("${SPRING_REDIS_PORT:6379}")
    private int fallbackRedisPort;

    @Value("${SPRING_REDIS_SSL:false}")
    private String fallbackSslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        System.out.println("=== RedisConfig: Initializing Redis Connection Factory ===");
        
        String redisHost = parameterStoreService.getParameter("/poc/redis/endpoint", fallbackRedisHost);
        String redisPortStr = parameterStoreService.getParameter("/poc/redis/port", String.valueOf(fallbackRedisPort));
        String sslEnabledStr = parameterStoreService.getParameter("/poc/redis/ssl", fallbackSslEnabled);
        
        int redisPort = Integer.parseInt(redisPortStr);
        boolean sslEnabled = Boolean.parseBoolean(sslEnabledStr);
        
        System.out.println("=== RedisConfig: Host=" + redisHost + " Port=" + redisPort + " SSL=" + sslEnabled + " ===");
        
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(5))
            .shutdownTimeout(Duration.ofSeconds(2));
        
        if (sslEnabled) {
            System.out.println("=== RedisConfig: Enabling SSL/TLS for ElastiCache Serverless ===");
            clientConfigBuilder.useSsl()
                .and()
                .clientOptions(ClientOptions.builder()
                    .autoReconnect(true)
                    .pingBeforeActivateConnection(false)
                    .sslOptions(SslOptions.builder()
                        .jdkSslProvider()
                        .build())
                    .build());
        } else {
            System.out.println("=== RedisConfig: SSL disabled - using plain connection ===");
            clientConfigBuilder.clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .pingBeforeActivateConnection(false)
                .build());
        }
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        return new LettuceConnectionFactory(config, clientConfigBuilder.build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}

package com.poc.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisServerlessConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisServerlessConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.ssl.enabled:true}")
    private boolean useSsl;

    @Bean
    public ClientResources clientResources() {
        return DefaultClientResources.builder().build();
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfig(ClientResources clientResources) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .clientOptions(
                    ClientOptions.builder()
                        .autoReconnect(true)
                        .pingBeforeActivateConnection(true)
                        .protocolVersion(ProtocolVersion.RESP3)
                        .socketOptions(
                            SocketOptions.builder()
                                .connectTimeout(Duration.ofSeconds(5))
                                .keepAlive(true)
                                .tcpNoDelay(true)
                                .build()
                        )
                        .timeoutOptions(
                            TimeoutOptions.builder()
                                .fixedTimeout(Duration.ofSeconds(3))
                                .build()
                        )
                        .build()
                )
                .clientResources(clientResources)
                .commandTimeout(Duration.ofSeconds(3))
                .shutdownTimeout(Duration.ofSeconds(2));
        
        if (useSsl) {
            builder.useSsl();
        }
        
        return builder.build();
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(LettuceClientConfiguration clientConfig) {
        log.info("Configuring Redis Serverless connection to: {}:{}, SSL: {}", redisHost, redisPort, useSsl);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.setValidateConnection(true);
        
        return factory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();
        
        return template;
    }
}

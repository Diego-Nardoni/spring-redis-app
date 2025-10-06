package com.modernapp.integration;

import com.modernapp.dto.SessionResponse;
import com.modernapp.service.RedisHealthService;
import com.modernapp.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisHealthService redisHealthService;

    @Autowired
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        // Clean Redis before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void shouldStartRedisContainer() {
        assertThat(redis.isRunning()).isTrue();
        assertThat(redis.getFirstMappedPort()).isGreaterThan(0);
    }

    @Test
    void shouldConnectToRedis() {
        SessionResponse.HealthStatus health = redisHealthService.getHealthStatus();
        
        assertThat(health.isConnected()).isTrue();
        assertThat(health.getStatus()).isEqualTo("HEALTHY");
        assertThat(health.getResponseTimeMs()).isGreaterThan(0);
    }

    @Test
    void shouldPerformRedisOperations() {
        // Test basic Redis operations
        String key = "test:key";
        String value = "test-value";
        
        redisTemplate.opsForValue().set(key, value);
        String retrievedValue = (String) redisTemplate.opsForValue().get(key);
        
        assertThat(retrievedValue).isEqualTo(value);
    }

    @Test
    void shouldPassPerformanceTest() {
        boolean result = redisHealthService.testRedisPerformance();
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetRedisInfo() {
        Map<String, Object> info = redisHealthService.getRedisInfo();
        
        assertThat(info).isNotEmpty();
        assertThat(info).containsKey("version");
        assertThat(info).containsKey("uptime");
        assertThat(info).containsKey("connected_clients");
    }

    @Test
    void shouldCreateSessionViaAPI() {
        String url = "http://localhost:" + port + "/api/session";
        
        ResponseEntity<SessionResponse> response = restTemplate.getForEntity(url, SessionResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSessionId()).isNotNull();
        assertThat(response.getBody().getUserId()).isNotNull();
        assertThat(response.getBody().getRedisHealth().isConnected()).isTrue();
    }

    @Test
    void shouldSetSessionAttribute() {
        String url = "http://localhost:" + port + "/api/session/attribute?key=testKey&value=testValue";
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("key")).isEqualTo("testKey");
        assertThat(response.getBody().get("value")).isEqualTo("testValue");
    }

    @Test
    void shouldLoadDashboard() {
        String url = "http://localhost:" + port + "/";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Modern Redis Session Manager");
        assertThat(response.getBody()).contains("Session Overview");
        assertThat(response.getBody()).contains("Redis Health");
    }

    @Test
    void shouldProvidePrometheusMetrics() {
        String url = "http://localhost:" + port + "/actuator/prometheus";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jvm_memory_used_bytes");
        assertThat(response.getBody()).contains("http_server_requests");
    }

    @Test
    void shouldProvideHealthEndpoint() {
        String url = "http://localhost:" + port + "/actuator/health";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        
        Map<String, Object> components = (Map<String, Object>) response.getBody().get("components");
        assertThat(components).containsKey("redis");
        
        Map<String, Object> redisHealth = (Map<String, Object>) components.get("redis");
        assertThat(redisHealth.get("status")).isEqualTo("UP");
    }
}

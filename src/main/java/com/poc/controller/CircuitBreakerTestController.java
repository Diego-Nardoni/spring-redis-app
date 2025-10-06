package com.poc.controller;

import com.poc.service.RedisService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/circuit-breaker")
public class CircuitBreakerTestController {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-service");
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        
        status.put("circuitBreakerState", circuitBreaker.getState().toString());
        status.put("failureRate", metrics.getFailureRate());
        status.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        status.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        status.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        status.put("localCacheStatus", redisService.getLocalCacheStatus());
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/test-redis/{key}")
    public ResponseEntity<Map<String, Object>> testRedis(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "test-value") String value) {
        
        Map<String, Object> result = new HashMap<>();
        
        // Testa escrita (circuit breaker vai interceptar se necessário)
        redisService.setValue(key, value, Duration.ofMinutes(5));
        result.put("writeStatus", "SUCCESS");
        
        // Testa leitura (circuit breaker vai interceptar se necessário)
        Object readValue = redisService.getValue(key);
        result.put("readStatus", "SUCCESS");
        result.put("readValue", readValue);
        
        // Testa conexão (circuit breaker vai interceptar se necessário)
        String connectionStatus = redisService.checkConnection();
        result.put("connectionStatus", connectionStatus);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/test-redis/{key}")
    public ResponseEntity<Map<String, Object>> getFromRedis(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        
        Object value = redisService.getValue(key);
        result.put("key", key);
        result.put("value", value);
        result.put("status", "SUCCESS");
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/test-redis/{key}")
    public ResponseEntity<Map<String, Object>> deleteFromRedis(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        
        Boolean deleted = redisService.deleteKey(key);
        result.put("key", key);
        result.put("deleted", deleted);
        result.put("status", "SUCCESS");
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/force-circuit-open")
    public ResponseEntity<Map<String, Object>> forceCircuitOpen() {
        Map<String, Object> result = new HashMap<>();
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-service");
        circuitBreaker.transitionToOpenState();
        
        result.put("message", "Circuit Breaker forçado para estado OPEN");
        result.put("newState", circuitBreaker.getState().toString());
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/force-circuit-closed")
    public ResponseEntity<Map<String, Object>> forceCircuitClosed() {
        Map<String, Object> result = new HashMap<>();
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redis-service");
        circuitBreaker.transitionToClosedState();
        
        result.put("message", "Circuit Breaker forçado para estado CLOSED");
        result.put("newState", circuitBreaker.getState().toString());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/simple-test/{key}")
    public ResponseEntity<Map<String, Object>> simpleTest(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        
        // Teste simples de leitura que vai usar fallback se circuit estiver aberto
        Object value = redisService.getValue(key);
        result.put("key", key);
        result.put("value", value);
        result.put("source", value != null ? "redis_or_cache" : "not_found");
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/clear-local-cache")
    public ResponseEntity<Map<String, Object>> clearLocalCache() {
        Map<String, Object> result = new HashMap<>();
        
        redisService.clearLocalCache();
        result.put("message", "Cache local limpo com sucesso");
        result.put("localCacheStatus", redisService.getLocalCacheStatus());
        
        return ResponseEntity.ok(result);
    }
}

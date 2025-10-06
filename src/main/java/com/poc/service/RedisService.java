package com.poc.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Cache local para fallback
    private final Map<String, Object> localCache = new HashMap<>();
    
    @CircuitBreaker(name = "redis-service", fallbackMethod = "getValueFallback")
    @RateLimiter(name = "api-limiter")
    @Retry(name = "redis-retry")
    public Object getValue(String key) {
        logger.info("Tentando buscar chave no Redis: {}", key);
        Object value = redisTemplate.opsForValue().get(key);
        
        // Se encontrou no Redis, atualiza cache local
        if (value != null) {
            localCache.put(key, value);
            logger.info("Valor encontrado no Redis e salvo no cache local: {}", key);
        }
        
        return value;
    }
    
    @CircuitBreaker(name = "redis-service", fallbackMethod = "setValueFallback")
    @RateLimiter(name = "api-limiter")
    @Retry(name = "redis-retry")
    public void setValue(String key, Object value, Duration ttl) {
        logger.info("Tentando salvar chave no Redis: {} com TTL: {}", key, ttl);
        
        if (ttl != null) {
            redisTemplate.opsForValue().set(key, value, ttl.getSeconds(), TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
        
        // Atualiza cache local também
        localCache.put(key, value);
        logger.info("Valor salvo no Redis e cache local: {}", key);
    }
    
    @CircuitBreaker(name = "redis-service", fallbackMethod = "deleteKeyFallback")
    @RateLimiter(name = "api-limiter")
    @Retry(name = "redis-retry")
    public Boolean deleteKey(String key) {
        logger.info("Tentando deletar chave do Redis: {}", key);
        Boolean deleted = redisTemplate.delete(key);
        
        // Remove do cache local também
        localCache.remove(key);
        logger.info("Chave deletada do Redis e cache local: {}", key);
        
        return deleted;
    }
    
    @CircuitBreaker(name = "redis-service", fallbackMethod = "checkConnectionFallback")
    public String checkConnection() {
        logger.info("Verificando conexão com Redis");
        
        // Tenta fazer um ping no Redis
        String pong = redisTemplate.getConnectionFactory().getConnection().ping();
        logger.info("Redis respondeu: {}", pong);
        
        return "Redis conectado: " + pong;
    }
    
    // ========== MÉTODOS DE FALLBACK ==========
    
    public Object getValueFallback(String key, Exception ex) {
        logger.warn("Circuit Breaker ATIVO - Usando cache local para chave: {}. Erro: {}", 
                   key, ex.getMessage());
        
        Object cachedValue = localCache.get(key);
        if (cachedValue != null) {
            logger.info("Valor encontrado no cache local: {}", key);
            return cachedValue;
        }
        
        logger.warn("Chave não encontrada no cache local: {}", key);
        return null;
    }
    
    public void setValueFallback(String key, Object value, Duration ttl, Exception ex) {
        logger.warn("Circuit Breaker ATIVO - Salvando apenas no cache local: {}. Erro: {}", 
                   key, ex.getMessage());
        
        // Salva apenas no cache local quando Redis está indisponível
        localCache.put(key, value);
        logger.info("Valor salvo apenas no cache local: {}", key);
    }
    
    public Boolean deleteKeyFallback(String key, Exception ex) {
        logger.warn("Circuit Breaker ATIVO - Removendo apenas do cache local: {}. Erro: {}", 
                   key, ex.getMessage());
        
        // Remove apenas do cache local
        Object removed = localCache.remove(key);
        logger.info("Chave removida apenas do cache local: {}", key);
        
        return removed != null;
    }
    
    public String checkConnectionFallback(Exception ex) {
        logger.error("Circuit Breaker ATIVO - Redis indisponível. Erro: {}", ex.getMessage());
        return "Redis indisponível - Circuit Breaker OPEN. Cache local ativo com " + 
               localCache.size() + " itens.";
    }
    
    // ========== MÉTODOS UTILITÁRIOS ==========
    
    public Map<String, Object> getLocalCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("localCacheSize", localCache.size());
        status.put("localCacheKeys", localCache.keySet());
        return status;
    }
    
    public void clearLocalCache() {
        localCache.clear();
        logger.info("Cache local limpo");
    }
}

package com.poc.controller;

import com.poc.service.ParameterStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parameter-store")
public class ParameterStoreController {

    @Autowired
    private ParameterStoreService parameterStoreService;

    @GetMapping("/redis-config")
    public ResponseEntity<Map<String, Object>> getRedisConfig() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            String endpoint = parameterStoreService.getParameter("/poc/redis/endpoint");
            String port = parameterStoreService.getParameter("/poc/redis/port");
            
            config.put("status", "success");
            config.put("redis_endpoint", endpoint);
            config.put("redis_port", port);
            config.put("source", "AWS Parameter Store");
            
        } catch (Exception e) {
            config.put("status", "error");
            config.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(config);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testParameterStore() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Testar parâmetros existentes
            String redisEndpoint = parameterStoreService.getParameter("/poc/redis/endpoint", "not-found");
            String redisPort = parameterStoreService.getParameter("/poc/redis/port", "not-found");
            
            // Testar parâmetro inexistente
            String nonExistent = parameterStoreService.getParameter("/poc/non-existent", "default-value");
            
            result.put("status", "success");
            result.put("redis_endpoint", redisEndpoint);
            result.put("redis_port", redisPort);
            result.put("non_existent_param", nonExistent);
            result.put("message", "Parameter Store connection working");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}

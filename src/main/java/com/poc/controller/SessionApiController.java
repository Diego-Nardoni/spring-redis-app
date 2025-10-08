package com.poc.controller;

import com.poc.model.SessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/session")
public class SessionApiController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/test")
    public SessionInfo testSession(HttpSession session) {
        String sessionId = session.getId();
        
        // Increment counter
        Integer counter = (Integer) session.getAttribute("counter");
        if (counter == null) {
            counter = 0;
        }
        counter++;
        session.setAttribute("counter", counter);
        
        // Store additional data
        session.setAttribute("lastAccess", Instant.now().toString());
        session.setAttribute("containerInfo", getContainerInfo());
        
        return SessionInfo.builder()
                .sessionId(sessionId)
                .counter(counter)
                .isNew(session.isNew())
                .creationTime(session.getCreationTime())
                .lastAccessedTime(session.getLastAccessedTime())
                .containerInfo(getContainerInfo())
                .build();
    }

    @GetMapping("/info")
    public Map<String, Object> getSessionInfo(HttpSession session) {
        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", session.getId());
        info.put("isNew", session.isNew());
        info.put("creationTime", session.getCreationTime());
        info.put("lastAccessedTime", session.getLastAccessedTime());
        info.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        // Get all attributes
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(name -> 
            attributes.put(name, session.getAttribute(name))
        );
        info.put("attributes", attributes);
        
        return info;
    }

    @PostMapping("/data")
    public Map<String, Object> storeData(@RequestBody Map<String, Object> data, HttpSession session) {
        data.forEach(session::setAttribute);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("sessionId", session.getId());
        response.put("storedKeys", data.keySet());
        
        return response;
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clearSession(HttpSession session) {
        session.invalidate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "session cleared");
        response.put("timestamp", Instant.now().toString());
        
        return response;
    }

    @GetMapping("/redis/test")
    public Map<String, Object> testRedisDirectly() {
        String testKey = "test:connection:" + System.currentTimeMillis();
        String testValue = "Hello from " + getContainerInfo();
        
        try {
            // Test write
            redisTemplate.opsForValue().set(testKey, testValue, 60, TimeUnit.SECONDS);
            
            // Test read
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            // Test delete
            redisTemplate.delete(testKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("testKey", testKey);
            result.put("testValue", testValue);
            result.put("retrievedValue", retrievedValue);
            result.put("valuesMatch", testValue.equals(retrievedValue));
            result.put("containerInfo", getContainerInfo());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("containerInfo", getContainerInfo());
            
            return result;
        }
    }

    private String getContainerInfo() {
        try {
            String hostname = System.getenv("HOSTNAME");
            if (hostname == null) {
                hostname = java.net.InetAddress.getLocalHost().getHostName();
            }
            return hostname;
        } catch (Exception e) {
            return "unknown-container";
        }
    }
}

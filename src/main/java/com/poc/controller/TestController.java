package com.poc.controller;

import com.poc.service.ArchitectureTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ArchitectureTestService testService;

    // ESTRATÉGIA 1: Endpoint com timestamp para evitar cache
    @GetMapping("/container/{timestamp}")
    public ResponseEntity<Map<String, Object>> getContainerWithTimestamp(
            @PathVariable String timestamp,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("containerInfo", getContainerInfo());
        result.put("timestamp", timestamp);
        result.put("serverTime", Instant.now().toString());
        result.put("requestId", UUID.randomUUID().toString());
        result.put("clientIp", getClientIp(request));
        
        // Headers anti-cache
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        return ResponseEntity.ok().headers(headers).body(result);
    }

    // ESTRATÉGIA 2: Endpoint com query parameter aleatório
    @GetMapping("/loadbalance")
    public ResponseEntity<Map<String, Object>> testLoadBalancing(
            @RequestParam(defaultValue = "1") int requests,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("containerInfo", getContainerInfo());
        result.put("requestNumber", requests);
        result.put("timestamp", Instant.now().toString());
        result.put("randomId", UUID.randomUUID().toString());
        result.put("clientIp", getClientIp(request));
        result.put("headers", getCloudFrontHeaders(request));
        
        // Headers anti-cache
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("X-Request-ID", UUID.randomUUID().toString());
        
        return ResponseEntity.ok().headers(headers).body(result);
    }

    // ESTRATÉGIA 3: Endpoint POST (nunca é cacheado)
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> testSessionPost(
            @RequestBody(required = false) Map<String, Object> data,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("containerInfo", getContainerInfo());
        result.put("method", "POST");
        result.put("timestamp", Instant.now().toString());
        result.put("sessionId", request.getSession().getId());
        result.put("inputData", data);
        
        // Increment session counter
        Integer counter = (Integer) request.getSession().getAttribute("counter");
        if (counter == null) counter = 0;
        counter++;
        request.getSession().setAttribute("counter", counter);
        result.put("sessionCounter", counter);
        
        return ResponseEntity.ok(result);
    }

    // ESTRATÉGIA 4: Endpoint com headers customizados para bypass cache
    @GetMapping("/nocache/{id}")
    public ResponseEntity<Map<String, Object>> getNoCacheTest(
            @PathVariable String id,
            @RequestHeader(value = "X-Bypass-Cache", defaultValue = "false") String bypassCache,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("containerInfo", getContainerInfo());
        result.put("testId", id);
        result.put("bypassCache", bypassCache);
        result.put("timestamp", Instant.now().toString());
        result.put("cloudFrontHeaders", getCloudFrontHeaders(request));
        
        // Headers anti-cache mais agressivos
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate, private");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        headers.add("X-Accel-Expires", "0");
        headers.add("Vary", "*");
        
        return ResponseEntity.ok().headers(headers).body(result);
    }

    // ESTRATÉGIA 5: WebSocket-like polling endpoint
    @GetMapping("/poll")
    public ResponseEntity<Map<String, Object>> pollStatus(
            @RequestParam(defaultValue = "0") long lastUpdate,
            HttpServletRequest request) {
        
        long currentTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("containerInfo", getContainerInfo());
        result.put("currentTime", currentTime);
        result.put("lastUpdate", lastUpdate);
        result.put("timeDiff", currentTime - lastUpdate);
        result.put("architectureStatus", testService.getArchitectureStatus());
        
        // Headers para polling
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("X-Timestamp", String.valueOf(currentTime));
        
        return ResponseEntity.ok().headers(headers).body(result);
    }

    private String getContainerInfo() {
        try {
            String hostname = System.getenv("HOSTNAME");
            if (hostname == null) {
                hostname = java.net.InetAddress.getLocalHost().getHostName();
            }
            return hostname;
        } catch (Exception e) {
            return "unknown-container-" + System.currentTimeMillis();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Map<String, String> getCloudFrontHeaders(HttpServletRequest request) {
        Map<String, String> cfHeaders = new HashMap<>();
        
        // Headers importantes do CloudFront
        String[] cloudFrontHeaderNames = {
            "CloudFront-Forwarded-Proto",
            "CloudFront-Is-Desktop-Viewer",
            "CloudFront-Is-Mobile-Viewer",
            "CloudFront-Is-SmartTV-Viewer",
            "CloudFront-Is-Tablet-Viewer",
            "CloudFront-Viewer-Country",
            "Via",
            "X-Amz-Cf-Id",
            "X-Forwarded-For",
            "X-Forwarded-Port",
            "X-Forwarded-Proto"
        };
        
        for (String headerName : cloudFrontHeaderNames) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                cfHeaders.put(headerName, headerValue);
            }
        }
        
        return cfHeaders;
    }
}

package com.poc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {
    
    @GetMapping("/hello")
    public Map<String, Object> hello(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Get or create session data
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        visitCount++;
        session.setAttribute("visitCount", visitCount);
        session.setAttribute("lastVisit", LocalDateTime.now().toString());
        
        // Response with session info
        response.put("message", "Hello from Spring Boot with Redis Session!");
        response.put("sessionId", session.getId());
        response.put("visitCount", visitCount);
        response.put("lastVisit", session.getAttribute("lastVisit"));
        response.put("isNew", session.isNew());
        response.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        return response;
    }
    

}

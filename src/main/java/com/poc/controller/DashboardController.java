package com.poc.controller;

import com.poc.service.ArchitectureTestService;
import com.poc.model.ArchitectureStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    @Autowired
    private ArchitectureTestService testService;

    @GetMapping("/")
    public String dashboard(Model model, HttpServletRequest request) {
        // Container Info
        String containerInfo = getContainerInfo();
        String clientIp = getClientIp(request);
        
        // Architecture Status
        ArchitectureStatus status = testService.getArchitectureStatus();
        
        model.addAttribute("containerInfo", containerInfo);
        model.addAttribute("clientIp", clientIp);
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("status", status);
        
        return "dashboard";
    }

    @GetMapping("/api/status")
    @ResponseBody
    public ArchitectureStatus getStatus() {
        return testService.getArchitectureStatus();
    }

    @GetMapping("/api/container")
    @ResponseBody
    public String getContainerInfo() {
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

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

package com.poc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.net.InetAddress;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        String containerId = getContainerId();
        return "<html><body>" +
               "<h1>Spring Boot + Redis Session Demo</h1>" +
               "<p>Aplicação funcionando com ALB + ECS + Redis!</p>" +
               "<p><strong>Container ID:</strong> " + containerId + "</p>" +
               "<p><a href='/session'>Testar Sessão Redis</a></p>" +
               "</body></html>";
    }
    
    private String getContainerId() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return hostname.length() > 12 ? hostname.substring(0, 12) : hostname;
        } catch (Exception e) {
            return "unknown";
        }
    }
}

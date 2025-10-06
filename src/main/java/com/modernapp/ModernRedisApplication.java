package com.modernapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class ModernRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModernRedisApplication.class, args);
    }
}

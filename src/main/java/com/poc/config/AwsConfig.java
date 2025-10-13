package com.poc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;

import jakarta.annotation.PostConstruct;

@Configuration
@Profile("serverless")
public class AwsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsConfig.class);

    @PostConstruct
    public void init() {
        log.info("AWS Configuration initialized for region: {}", Region.US_EAST_1);
        log.info("Parameter Store integration enabled");
    }
}

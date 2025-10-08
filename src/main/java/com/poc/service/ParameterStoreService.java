package com.poc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

@Service
public class ParameterStoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParameterStoreService.class);
    
    private SsmClient ssmClient;
    
    public ParameterStoreService() {
        this.ssmClient = SsmClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
    
    public String getParameter(String parameterName) {
        return getParameter(parameterName, null);
    }
    
    public String getParameter(String parameterName, String defaultValue) {
        System.out.println("=== ParameterStoreService: Attempting to get parameter: " + parameterName + " ===");
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .build();
            
            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();
            
            System.out.println("=== ParameterStoreService: Retrieved parameter " + parameterName + " = " + value + " ===");
            logger.info("Retrieved parameter {} from Parameter Store", parameterName);
            return value;
            
        } catch (ParameterNotFoundException e) {
            System.out.println("=== ParameterStoreService: Parameter " + parameterName + " not found, using default: " + defaultValue + " ===");
            logger.warn("Parameter {} not found in Parameter Store, using default: {}", 
                       parameterName, defaultValue);
            return defaultValue;
        } catch (Exception e) {
            System.out.println("=== ParameterStoreService: Error retrieving parameter " + parameterName + ": " + e.getMessage() + " ===");
            logger.error("Error retrieving parameter {} from Parameter Store: {}", 
                        parameterName, e.getMessage());
            return defaultValue;
        }
    }
}

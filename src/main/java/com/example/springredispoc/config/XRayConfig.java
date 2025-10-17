package com.example.springredispoc.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ECSPlugin;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XRayConfig {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new EC2Plugin())
                .withPlugin(new ECSPlugin());
        
        // Use default sampling rules
        AWSXRay.setGlobalRecorder(builder.build());
    }

    // No Filter needed - X-Ray SDK will work automatically with Spring Boot 3+
    // Tracing will be handled by the SDK directly via environment variables
}

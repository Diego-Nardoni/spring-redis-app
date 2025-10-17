package com.example.springredispoc.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class XRayConfig {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new EC2Plugin())
                .withPlugin(new ECSPlugin());
        
        // Use default sampling rules
        AWSXRay.setGlobalRecorder(builder.build());
    }

    @Bean
    public Filter TracingFilter() {
        return new com.amazonaws.xray.javax.servlet.AWSXRayServletFilter("SpringRedisApp");
    }
}

package com.mfpe.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    @Bean
    public HealthIndicator orderflowHealthIndicator() {
        return () -> Health.up()
                .withDetail("app", "orderflow")
                .withDetail("version", "1.0-SNAPSHOT")
                .withDetail("description", "Order management service")
                .build();
    }

}

package com.observability.aiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class
 * 
 * This starts our Spring Boot application with:
 * - REST API endpoints
 * - OpenTelemetry distributed tracing
 * - Prometheus metrics export
 * - Actuator health checks
 */
@SpringBootApplication
public class AIObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIObservabilityApplication.class, args);
    }
}

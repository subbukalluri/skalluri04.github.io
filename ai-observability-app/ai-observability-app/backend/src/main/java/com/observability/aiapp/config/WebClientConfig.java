package com.observability.aiapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient Configuration
 * 
 * WebClient is Spring's modern HTTP client for making API calls.
 * It's non-blocking and supports reactive programming.
 * 
 * We'll use this to call the Claude AI API.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)); // 16MB buffer for large responses
    }
}

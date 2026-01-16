package com.observability.aiapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Service - Handles sentiment analysis using Claude AI
 * 
 * This service demonstrates:
 * 1. Distributed Tracing with OpenTelemetry
 * 2. Metrics collection with Micrometer
 * 3. Structured logging
 * 4. External API integration
 */
@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    private final WebClient webClient;
    private final Tracer tracer;
    private final ObjectMapper objectMapper;
    
    // Metrics
    private final Counter aiRequestCounter;
    private final Counter aiSuccessCounter;
    private final Counter aiErrorCounter;
    private final Timer aiResponseTimer;

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String aiApiKey;

    @Value("${ai.api.model}")
    private String aiModel;

    @Value("${ai.api.max-tokens}")
    private int maxTokens;

    public AIService(WebClient.Builder webClientBuilder, 
                     Tracer tracer, 
                     MeterRegistry meterRegistry) {
        this.webClient = webClientBuilder.build();
        this.tracer = tracer;
        this.objectMapper = new ObjectMapper();
        
        // Initialize metrics
        this.aiRequestCounter = Counter.builder("ai.requests.total")
                .description("Total number of AI API requests")
                .register(meterRegistry);
                
        this.aiSuccessCounter = Counter.builder("ai.requests.success")
                .description("Successful AI API requests")
                .register(meterRegistry);
                
        this.aiErrorCounter = Counter.builder("ai.requests.error")
                .description("Failed AI API requests")
                .register(meterRegistry);
                
        this.aiResponseTimer = Timer.builder("ai.response.time")
                .description("AI API response time")
                .register(meterRegistry);
    }

    /**
     * Analyze sentiment using Claude AI
     * This method creates a child span to track the AI API call
     */
    public Mono<Map<String, Object>> analyzeSentiment(String text) {
        // Increment request counter
        aiRequestCounter.increment();
        
        // Create a span for this operation
        Span span = tracer.spanBuilder("ai.analyze_sentiment")
                .startSpan();
        
        // Add attributes to the span for better tracing
        span.setAttribute("ai.model", aiModel);
        span.setAttribute("ai.input.length", text.length());
        
        try (Scope scope = span.makeCurrent()) {
            logger.info("Starting sentiment analysis for text: {}", 
                    text.substring(0, Math.min(50, text.length())));
            
            // Record the time taken for AI API call
            return aiResponseTimer.record(() -> 
                callClaudeAPI(text)
                    .doOnSuccess(response -> {
                        aiSuccessCounter.increment();
                        span.setAttribute("ai.output.sentiment", 
                                (String) response.get("sentiment"));
                        span.setStatus(StatusCode.OK);
                        logger.info("Sentiment analysis completed successfully");
                    })
                    .doOnError(error -> {
                        aiErrorCounter.increment();
                        span.recordException(error);
                        span.setStatus(StatusCode.ERROR, error.getMessage());
                        logger.error("Error during sentiment analysis", error);
                    })
                    .doFinally(signalType -> span.end())
            );
        }
    }

    /**
     * Makes the actual API call to Claude
     */
    private Mono<Map<String, Object>> callClaudeAPI(String text) {
        // Create a child span for the HTTP call
        Span httpSpan = tracer.spanBuilder("http.call.claude")
                .startSpan();
        
        try (Scope scope = httpSpan.makeCurrent()) {
            // Build the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel);
            requestBody.put("max_tokens", maxTokens);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", 
                "Analyze the sentiment of this text and respond in JSON format with: " +
                "{\"sentiment\": \"positive/negative/neutral\", " +
                "\"confidence\": 0.0-1.0, " +
                "\"analysis\": \"brief explanation\"}. " +
                "Text: " + text);
            
            requestBody.put("messages", List.of(message));

            httpSpan.setAttribute("http.method", "POST");
            httpSpan.setAttribute("http.url", aiApiUrl);

            return webClient.post()
                    .uri(aiApiUrl)
                    .header("x-api-key", aiApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .map(this::parseAIResponse)
                    .doOnSuccess(response -> {
                        httpSpan.setStatus(StatusCode.OK);
                        httpSpan.end();
                    })
                    .doOnError(error -> {
                        httpSpan.recordException(error);
                        httpSpan.setStatus(StatusCode.ERROR, error.getMessage());
                        httpSpan.end();
                    });
        }
    }

    /**
     * Parse the AI response and extract sentiment information
     */
    private Map<String, Object> parseAIResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("content").get(0);
            String textResponse = content.path("text").asText();
            
            // Extract JSON from the text response
            int jsonStart = textResponse.indexOf("{");
            int jsonEnd = textResponse.lastIndexOf("}") + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = textResponse.substring(jsonStart, jsonEnd);
                JsonNode sentimentData = objectMapper.readTree(jsonStr);
                
                Map<String, Object> result = new HashMap<>();
                result.put("sentiment", sentimentData.path("sentiment").asText("neutral"));
                result.put("confidence", sentimentData.path("confidence").asDouble(0.5));
                result.put("analysis", sentimentData.path("analysis").asText("No analysis available"));
                
                return result;
            }
            
            // Fallback response
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("sentiment", "neutral");
            fallback.put("confidence", 0.5);
            fallback.put("analysis", "Unable to parse sentiment");
            return fallback;
            
        } catch (Exception e) {
            logger.error("Error parsing AI response", e);
            Map<String, Object> error = new HashMap<>();
            error.put("sentiment", "error");
            error.put("confidence", 0.0);
            error.put("analysis", "Error processing response");
            return error;
        }
    }
}

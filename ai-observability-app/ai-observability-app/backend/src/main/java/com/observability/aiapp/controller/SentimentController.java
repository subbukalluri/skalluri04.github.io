package com.observability.aiapp.controller;

import com.observability.aiapp.dto.SentimentRequest;
import com.observability.aiapp.dto.SentimentResponse;
import com.observability.aiapp.service.AIService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Sentiment Analysis Controller
 * 
 * This is the entry point for our AI application.
 * It receives requests from the frontend and orchestrates the sentiment analysis.
 * 
 * Key features:
 * - Automatic span creation for each HTTP request (via OpenTelemetry auto-instrumentation)
 * - Manual span creation for business logic
 * - Metrics collection
 * - Structured logging with trace context
 */
@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin(origins = "http://localhost:3000")
public class SentimentController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);
    
    private final AIService aiService;
    private final Tracer tracer;
    private final Counter endpointCounter;

    public SentimentController(AIService aiService, 
                              Tracer tracer,
                              MeterRegistry meterRegistry) {
        this.aiService = aiService;
        this.tracer = tracer;
        this.endpointCounter = Counter.builder("endpoint.requests")
                .description("Number of requests to sentiment endpoint")
                .tag("endpoint", "/api/sentiment/analyze")
                .register(meterRegistry);
    }

    /**
     * Analyze sentiment endpoint
     * 
     * This endpoint:
     * 1. Receives text from frontend
     * 2. Creates a span to track the operation
     * 3. Calls AI service for sentiment analysis
     * 4. Returns the result with trace information
     */
    @PostMapping("/analyze")
    public Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(
            @RequestBody SentimentRequest request) {
        
        // Increment endpoint counter
        endpointCounter.increment();
        
        // Get the current span (automatically created by OpenTelemetry)
        Span currentSpan = Span.current();
        String traceId = currentSpan.getSpanContext().getTraceId();
        
        // Add custom attributes to the span
        currentSpan.setAttribute("request.text.length", request.getText().length());
        currentSpan.setAttribute("endpoint", "/api/sentiment/analyze");
        
        logger.info("Received sentiment analysis request. TraceId: {}", traceId);
        
        long startTime = System.currentTimeMillis();
        
        // Create a child span for the controller logic
        Span controllerSpan = tracer.spanBuilder("controller.process_request")
                .startSpan();
        
        try (var scope = controllerSpan.makeCurrent()) {
            return aiService.analyzeSentiment(request.getText())
                    .map(result -> {
                        long processingTime = System.currentTimeMillis() - startTime;
                        
                        // Build response with trace information
                        SentimentResponse response = SentimentResponse.builder()
                                .sentiment((String) result.get("sentiment"))
                                .confidence((Double) result.get("confidence"))
                                .analysis((String) result.get("analysis"))
                                .processingTimeMs(processingTime)
                                .traceId(traceId)
                                .build();
                        
                        controllerSpan.setAttribute("response.sentiment", response.getSentiment());
                        controllerSpan.setAttribute("response.processing_time_ms", processingTime);
                        
                        logger.info("Sentiment analysis completed in {}ms. Sentiment: {}", 
                                processingTime, response.getSentiment());
                        
                        return ResponseEntity.ok(response);
                    })
                    .doFinally(signalType -> controllerSpan.end());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Sentiment Analyzer"
        ));
    }
}

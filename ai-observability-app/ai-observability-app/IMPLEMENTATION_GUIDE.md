# Implementation Guide: AI Observability Application

This guide walks you through implementing this project step-by-step, explaining every concept in detail.

## 📚 Table of Contents
1. [Understanding the Architecture](#understanding-the-architecture)
2. [Backend Implementation](#backend-implementation)
3. [Frontend Implementation](#frontend-implementation)
4. [Observability Stack Setup](#observability-stack-setup)
5. [Testing and Validation](#testing-and-validation)

---

## Understanding the Architecture

### What is Observability?

**Observability** is the ability to understand what's happening inside your system by examining its outputs. The three pillars are:

1. **Logs**: Individual events (e.g., "Request received at 10:30:45")
2. **Metrics**: Numerical measurements over time (e.g., "95th percentile latency: 250ms")
3. **Traces**: Request flows through distributed systems (e.g., "Request took 500ms: 50ms in frontend, 450ms in backend")

### Why Distributed Tracing?

In modern applications, a single user request might:
- Start in a browser
- Go to a load balancer
- Hit an API gateway
- Call multiple microservices
- Query databases
- Call external APIs

**Without tracing**: You only see errors, no context
**With tracing**: You see the exact path, timing, and where it failed

### OpenTelemetry Concepts

**Trace**: A trace is the entire journey of a request
```
Trace ID: abc123...
  ├─ Span 1: User clicks button (100ms)
  ├─ Span 2: HTTP request to backend (450ms)
  │   ├─ Span 3: Process in controller (50ms)
  │   └─ Span 4: Call AI API (380ms)
  └─ Span 5: Render result (50ms)
Total: 600ms
```

**Span**: A single operation or unit of work
- Has start time and duration
- Can have parent span (forming a hierarchy)
- Contains attributes (metadata)
- Can record events and exceptions

**Context Propagation**: Passing trace information between services
```
Browser Request Headers:
  traceparent: 00-abc123...-def456...-01
              ^^  ^^^^^^^   ^^^^^^^   ^^
              |   |         |         sampling
              |   |         span id
              |   trace id
              version
```

---

## Backend Implementation

### Step 1: Understanding Spring Boot with Actuator

**Spring Boot Actuator** provides production-ready features:

```java
// When you add spring-boot-starter-actuator dependency,
// you automatically get these endpoints:

// Health check
GET /actuator/health
Response: {"status":"UP"}

// Prometheus metrics
GET /actuator/prometheus
Response: 
# HELP jvm_memory_used_bytes
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 524288000
...
```

**Why it matters**: Actuator exposes internal application state that Prometheus can scrape.

### Step 2: OpenTelemetry Configuration

```java
@Configuration
public class OpenTelemetryConfig {
    @Bean
    public OpenTelemetry openTelemetry() {
        // 1. Create a Resource (identifies your service)
        Resource resource = Resource.create(
            Attributes.of(
                SERVICE_NAME, "ai-sentiment-analyzer",
                SERVICE_VERSION, "1.0.0"
            )
        );
        
        // 2. Create an Exporter (where to send traces)
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://jaeger:4317")  // Jaeger's OTLP receiver
            .build();
        
        // 3. Create a Span Processor (batches spans for efficiency)
        BatchSpanProcessor processor = BatchSpanProcessor.builder(exporter)
            .setMaxQueueSize(2048)
            .setScheduleDelay(Duration.ofSeconds(5))
            .build();
        
        // 4. Build and register
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(processor)
                    .setResource(resource)
                    .build()
            )
            .buildAndRegisterGlobal();
    }
}
```

**What happens**:
1. Your app creates spans
2. Spans are batched every 5 seconds
3. Batch is sent to Jaeger via gRPC
4. Jaeger stores and indexes the traces

### Step 3: Creating Spans in Services

```java
@Service
public class AIService {
    private final Tracer tracer;
    
    public Mono<Map<String, Object>> analyzeSentiment(String text) {
        // Start a span
        Span span = tracer.spanBuilder("ai.analyze_sentiment")
            .startSpan();
        
        // Make the span active (thread-local context)
        try (Scope scope = span.makeCurrent()) {
            // Add attributes
            span.setAttribute("ai.model", "claude-sonnet-4");
            span.setAttribute("input.length", text.length());
            
            // Your business logic
            return callAI(text)
                .doOnSuccess(result -> {
                    span.setAttribute("sentiment", result.get("sentiment"));
                    span.setStatus(StatusCode.OK);
                })
                .doOnError(error -> {
                    span.recordException(error);
                    span.setStatus(StatusCode.ERROR, error.getMessage());
                })
                .doFinally(signal -> span.end());
        }
    }
}
```

**Span Lifecycle**:
1. `startSpan()`: Creates span, records start time
2. `makeCurrent()`: Sets as active span in this thread
3. `setAttribute()`: Adds metadata
4. `setStatus()`: Marks success/failure
5. `end()`: Records end time, sends to processor

### Step 4: Metrics with Micrometer

```java
@Service
public class AIService {
    private final Counter requestCounter;
    private final Timer responseTimer;
    
    public AIService(MeterRegistry registry) {
        // Counter: monotonically increasing
        this.requestCounter = Counter.builder("ai.requests")
            .description("Total AI requests")
            .tag("service", "sentiment")
            .register(registry);
        
        // Timer: records duration histogram
        this.responseTimer = Timer.builder("ai.response.time")
            .description("AI response time")
            .register(registry);
    }
    
    public Mono<Response> analyze(String text) {
        requestCounter.increment();  // Count this request
        
        return responseTimer.record(() ->  // Time this operation
            doAnalysis(text)
        );
    }
}
```

**Prometheus Output**:
```
# Counter becomes a rate metric
ai_requests_total{service="sentiment"} 1523

# Timer creates multiple metrics
ai_response_time_seconds_count 1523
ai_response_time_seconds_sum 456.789
ai_response_time_seconds_max 2.345
ai_response_time_seconds_bucket{le="0.1"} 234
ai_response_time_seconds_bucket{le="0.5"} 1234
ai_response_time_seconds_bucket{le="1.0"} 1456
ai_response_time_seconds_bucket{le="+Inf"} 1523
```

---

## Frontend Implementation

### Step 1: Browser Tracing Setup

```javascript
// tracing.js
import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';

export function initTelemetry() {
    // 1. Create provider
    const provider = new WebTracerProvider({
        resource: new Resource({
            'service.name': 'ai-sentiment-frontend',
        }),
    });
    
    // 2. Configure exporter (sends to Jaeger via HTTP)
    const exporter = new OTLPTraceExporter({
        url: 'http://localhost:4318/v1/traces',
    });
    
    provider.addSpanProcessor(new SimpleSpanProcessor(exporter));
    provider.register();
    
    // 3. Auto-instrument fetch API
    registerInstrumentations({
        instrumentations: [
            new FetchInstrumentation({
                // Propagate trace context
                propagateTraceHeaderCorsUrls: [
                    'http://localhost:8080',
                ],
                // Add custom attributes
                applyCustomAttributesOnSpan: (span, request, response) => {
                    span.setAttribute('http.url', request.url);
                    span.setAttribute('http.status_code', response.status);
                },
            }),
        ],
    });
}
```

**What this does**:
- Automatically creates spans for all `fetch()` calls
- Adds `traceparent` header to outgoing requests
- Sends spans to Jaeger HTTP endpoint

### Step 2: Manual Spans in React

```javascript
function SentimentAnalyzer() {
    const tracer = getTracer();
    
    const handleAnalyze = async () => {
        // Create a span for user action
        const span = tracer.startSpan('user.analyze_sentiment');
        
        try {
            span.setAttribute('input.length', text.length);
            
            // This fetch is auto-instrumented
            // It will be a child span of 'user.analyze_sentiment'
            const response = await fetch('/api/sentiment/analyze', {
                method: 'POST',
                body: JSON.stringify({ text }),
            });
            
            const result = await response.json();
            span.setAttribute('result.sentiment', result.sentiment);
            span.setStatus({ code: 1 }); // OK
            
        } catch (error) {
            span.recordException(error);
            span.setStatus({ code: 2, message: error.message });
        } finally {
            span.end();
        }
    };
}
```

---

## Observability Stack Setup

### Jaeger Architecture

```
┌─────────────┐
│   Client    │ (Your app)
│ (OTLP SDK)  │
└──────┬──────┘
       │ gRPC/HTTP
       │ Port 4317/4318
       ▼
┌─────────────┐
│   Jaeger    │
│  Collector  │ (Receives spans)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Storage   │ (Badger/Cassandra/Elasticsearch)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Query UI   │ (Web interface)
│ Port 16686  │
└─────────────┘
```

### Prometheus Scraping

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'  # Where to scrape
    scrape_interval: 15s                  # How often
    static_configs:
      - targets: ['backend:8080']         # What to scrape
```

**Scrape Process**:
1. Every 15 seconds, Prometheus makes HTTP GET to backend:8080/actuator/prometheus
2. Backend returns current metric values in Prometheus format
3. Prometheus stores as time-series data
4. You can query: `rate(http_requests_total[5m])`

### Grafana Dashboard Creation

**Manual Steps** (do this once):
1. Open Grafana: http://localhost:3001
2. Go to **Dashboards** → **Import**
3. Upload `grafana/dashboards/ai-dashboard.json`
4. Dashboard automatically appears

**How it works**:
```json
{
  "targets": [
    {
      // PromQL query
      "expr": "rate(http_server_requests_seconds_count[5m])"
    }
  ]
}
```

Grafana:
1. Runs PromQL query against Prometheus
2. Gets time-series data
3. Renders as graph/stat/gauge

---

## Testing and Validation

### Test Flow

```bash
# 1. Start everything
cd docker && docker-compose up -d
cd ../frontend && npm start

# 2. In browser, analyze text
# "I love this product!"

# 3. Check logs
docker-compose logs backend | grep "traceId"

# 4. Find trace in Jaeger
# Copy traceId from response
# Open: http://localhost:16686
# Search by trace ID

# 5. View metrics
# Open: http://localhost:3001
# Go to AI Dashboard
```

### Understanding a Trace

```
Trace: abc123xyz789 (Total: 1.2s)

┌─ user.analyze_sentiment (Frontend) [1.2s]
│  Attributes:
│    - input.length: 50
│    - result.sentiment: positive
│
├─ HTTP POST /api/sentiment/analyze [1.15s]
│  Attributes:
│    - http.method: POST
│    - http.status_code: 200
│
│  ├─ controller.process_request [1.1s]
│  │  Attributes:
│  │    - endpoint: /api/sentiment/analyze
│  │
│  │  ├─ ai.analyze_sentiment [1.05s]
│  │  │  Attributes:
│  │  │    - ai.model: claude-sonnet-4
│  │  │    - ai.output.sentiment: positive
│  │  │
│  │  │  └─ http.call.claude [1.0s]
│  │  │     Attributes:
│  │  │       - http.url: https://api.anthropic.com/v1/messages
│  │  │       - http.status_code: 200
```

**What to look for**:
- Timing: Where is most time spent?
- Errors: Red spans indicate failures
- Attributes: Custom metadata you added
- Dependencies: Parent-child relationships

### Common Issues and Solutions

**Issue**: No traces appear in Jaeger
```bash
# Check if spans are being created
docker-compose logs backend | grep "span"

# Check if Jaeger is reachable
docker-compose exec backend curl -v jaeger:4317

# Check Jaeger UI
curl http://localhost:16686/api/services
```

**Issue**: No metrics in Prometheus
```bash
# Check if metrics endpoint works
curl http://localhost:8080/actuator/prometheus

# Check Prometheus targets
# Open: http://localhost:9090/targets
# Should show backend:8080 as UP
```

**Issue**: Grafana shows "No Data"
```bash
# Check if Prometheus is configured
curl http://localhost:3001/api/datasources

# Test query directly in Prometheus
# Open: http://localhost:9090
# Query: up{job="spring-boot-app"}
```

---

## Next Steps for Learning

### 1. Modify and Experiment
- Add more spans to track specific operations
- Create custom metrics for business KPIs
- Build new Grafana dashboards

### 2. Add Complexity
- Add a database layer
- Create multiple microservices
- Implement async messaging (Kafka/RabbitMQ)

### 3. Production Readiness
- Implement sampling strategies
- Add authentication/authorization
- Set up alerting rules
- Configure log aggregation

### 4. Advanced Topics
- Service mesh (Istio/Linkerd)
- Exemplars (linking metrics to traces)
- Distributed context propagation
- OpenTelemetry Collector

---

## Key Takeaways

✅ **Observability is essential** for understanding distributed systems
✅ **OpenTelemetry is the standard** for instrumentation
✅ **Traces show causality**, metrics show trends
✅ **Context propagation** connects distributed operations
✅ **Automatic instrumentation** reduces effort
✅ **Manual spans** provide business context

**Remember**: The goal isn't just to collect data, but to answer questions like:
- Why is this request slow?
- Which service is causing errors?
- What changed that affected performance?
- Is the system healthy right now?

Happy observing! 🔍

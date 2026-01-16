# AI Sentiment Analyzer with Full Observability Stack

A production-grade AI application demonstrating **complete observability** from frontend to backend to AI API, featuring:

- 🤖 **AI-Powered Sentiment Analysis** using Claude API
- 📊 **Distributed Tracing** with OpenTelemetry and Jaeger
- 📈 **Metrics Collection** with Prometheus
- 🎨 **Beautiful Dashboards** in Grafana
- 🔍 **End-to-End Visibility** across the entire stack

## 🏗️ Architecture Overview

```
┌─────────────┐     HTTP      ┌──────────────┐    AI API    ┌─────────────┐
│   React     │──────────────▶│  Spring Boot │─────────────▶│  Claude AI  │
│  Frontend   │               │   Backend    │              └─────────────┘
└─────────────┘               └──────────────┘
      │                              │
      │ OTLP                         │ OTLP
      │ Traces                       │ Traces
      ▼                              ▼
┌──────────────────────────────────────────┐
│              Jaeger                      │  ◀─── View Traces
│       (Distributed Tracing)              │
└──────────────────────────────────────────┘
                                   │
                                   │ Prometheus
                                   │ Metrics
                                   ▼
                          ┌─────────────────┐
                          │   Prometheus    │
                          │ (Metrics Store) │
                          └─────────────────┘
                                   │
                                   │ Query
                                   ▼
                          ┌─────────────────┐
                          │    Grafana      │  ◀─── Dashboards
                          │ (Visualization) │
                          └─────────────────┘
```

## 🎯 What This Demonstrates

### 1. **Distributed Tracing** (OpenTelemetry + Jaeger)
- Traces requests from browser through backend to AI API
- Automatic instrumentation of HTTP calls
- Manual span creation for business logic
- Trace context propagation across services
- Full request lifecycle visibility

### 2. **Metrics Collection** (Prometheus)
- Request rates and throughput
- Response time percentiles (P50, P95, P99)
- Success/error rates
- JVM metrics (memory, threads, GC)
- Custom business metrics (AI request counts)

### 3. **Visualization** (Grafana)
- Real-time dashboards
- Request rate graphs
- Latency heatmaps
- Error rate monitoring
- Resource utilization

### 4. **Production Patterns**
- Health checks
- Graceful error handling
- Structured logging with trace context
- Correlation IDs
- Metrics aggregation

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (for running observability stack)
- **Java 17+** and **Maven** (for backend)
- **Node.js 16+** and **npm** (for frontend)
- **Anthropic API Key** (get from https://console.anthropic.com)

### Step 1: Clone and Navigate
```bash
cd ai-observability-app
```

### Step 2: Set Up Environment
```bash
# Create .env file for API key
echo "ANTHROPIC_API_KEY=your-api-key-here" > docker/.env
```

### Step 3: Start Observability Stack
```bash
cd docker
docker-compose up -d

# Verify all services are running
docker-compose ps
```

This starts:
- **Jaeger** on http://localhost:16686
- **Prometheus** on http://localhost:9090
- **Grafana** on http://localhost:3001
- **Backend** on http://localhost:8080

### Step 4: Start Frontend
```bash
cd ../frontend
npm install
npm start
```

Frontend runs on http://localhost:3000

## 📱 Using the Application

### Access Points
| Service | URL | Credentials |
|---------|-----|-------------|
| **Application** | http://localhost:3000 | - |
| **Jaeger UI** | http://localhost:16686 | - |
| **Grafana** | http://localhost:3001 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **Backend API** | http://localhost:8080 | - |

### Test the Application
1. Open http://localhost:3000
2. Enter text like: *"I absolutely love this product! It exceeded all my expectations."*
3. Click **Analyze Sentiment**
4. Note the **Trace ID** in the response

### View the Trace
1. Copy the Trace ID
2. Open Jaeger: http://localhost:16686
3. Select service: `ai-sentiment-frontend` or `ai-sentiment-analyzer`
4. Click **Find Traces**
5. See the complete request flow:
   - Browser span (user action)
   - HTTP request span
   - Backend controller span
   - AI service span
   - External API call span

### View Metrics in Grafana
1. Open Grafana: http://localhost:3001
2. Login with: `admin` / `admin`
3. Navigate to **Dashboards** → **AI Sentiment Analyzer**
4. See real-time metrics:
   - Request rates
   - Response times
   - Success rates
   - JVM metrics

## 🔍 Understanding the Traces

### Trace Hierarchy
```
user.analyze_sentiment (Frontend)
  └─ HTTP POST /api/sentiment/analyze
      └─ controller.process_request (Backend)
          └─ ai.analyze_sentiment
              └─ http.call.claude
                  └─ [External API Call]
```

### Key Span Attributes
- `http.method`: HTTP method (POST, GET)
- `http.url`: Request URL
- `http.status_code`: Response status
- `ai.model`: AI model used
- `ai.input.length`: Input text length
- `ai.output.sentiment`: Detected sentiment
- `response.processing_time_ms`: Total processing time

## 📊 Key Metrics Explained

### Application Metrics
- **`ai_requests_total`**: Total AI API requests
- **`ai_requests_success`**: Successful requests
- **`ai_requests_error`**: Failed requests
- **`ai_response_time`**: AI API response time histogram
- **`endpoint_requests`**: HTTP endpoint hit counts

### JVM Metrics (Automatic)
- **`jvm_memory_used_bytes`**: Heap memory usage
- **`jvm_threads_live_threads`**: Active thread count
- **`jvm_gc_pause_seconds`**: Garbage collection pause time

### HTTP Metrics (Automatic)
- **`http_server_requests_seconds`**: Request duration histogram
- **`http_server_requests_seconds_count`**: Request count
- **`http_server_requests_seconds_sum`**: Total time spent

## 🛠️ Development Guide

### Adding Custom Metrics
```java
// In your service class
private final Counter customCounter;

public MyService(MeterRegistry registry) {
    this.customCounter = Counter.builder("my.custom.metric")
        .description("Description")
        .register(registry);
}

public void myMethod() {
    customCounter.increment();
}
```

### Adding Custom Spans
```java
// In your service class
private final Tracer tracer;

public void myMethod() {
    Span span = tracer.spanBuilder("my.operation").startSpan();
    try (Scope scope = span.makeCurrent()) {
        span.setAttribute("custom.attribute", "value");
        // Your business logic
        span.setStatus(StatusCode.OK);
    } finally {
        span.end();
    }
}
```

### Frontend Tracing
```javascript
import { getTracer } from './tracing';

const tracer = getTracer();
const span = tracer.startSpan('user.action');

try {
  span.setAttribute('user.input', input);
  await doSomething();
  span.setStatus({ code: 1 }); // OK
} catch (error) {
  span.recordException(error);
  span.setStatus({ code: 2, message: error.message });
} finally {
  span.end();
}
```

## 🎓 Learning Concepts

### 1. **OpenTelemetry**
- Industry-standard observability framework
- Vendor-neutral
- Supports traces, metrics, and logs
- Automatic and manual instrumentation

### 2. **Distributed Tracing**
- **Trace**: Complete request journey
- **Span**: Single operation/unit of work
- **Context Propagation**: Passing trace info between services
- **Baggage**: Metadata carried with trace

### 3. **Metrics Types**
- **Counter**: Monotonically increasing (requests, errors)
- **Gauge**: Point-in-time value (memory, CPU)
- **Histogram**: Distribution (latencies)
- **Summary**: Percentiles (P95, P99)

### 4. **Observability Pillars**
- **Logs**: What happened (events)
- **Metrics**: How much/many (aggregates)
- **Traces**: Why it happened (causality)

## 🔧 Troubleshooting

### Backend won't start
```bash
# Check if port 8080 is available
lsof -i :8080

# Check Docker logs
cd docker
docker-compose logs backend
```

### No traces in Jaeger
```bash
# Verify Jaeger is running
curl http://localhost:16686

# Check if backend can reach Jaeger
docker-compose exec backend curl jaeger:4317
```

### No metrics in Prometheus
```bash
# Check Actuator endpoint
curl http://localhost:8080/actuator/prometheus

# Verify Prometheus config
docker-compose exec prometheus cat /etc/prometheus/prometheus.yml
```

### Frontend traces not appearing
- Check browser console for CORS errors
- Verify OTLP endpoint: http://localhost:4318
- Clear browser cache and reload

## 📚 Best Practices Implemented

✅ **Separation of Concerns**: Tracing, metrics, and business logic are separate
✅ **Graceful Degradation**: App works even if telemetry fails
✅ **Context Propagation**: Trace IDs flow through all layers
✅ **Structured Logging**: Logs include trace context
✅ **Resource Attributes**: Services are properly identified
✅ **Sampling Strategy**: All traces captured (adjust for production)
✅ **Error Handling**: Exceptions recorded in spans
✅ **Health Checks**: Endpoints for monitoring
✅ **Metric Naming**: Clear, consistent naming conventions

## 🎯 Next Steps

1. **Add More Services**: Create microservices and trace them
2. **Implement Sampling**: Add trace sampling for high-volume scenarios
3. **Add Alerting**: Set up Grafana alerts for SLOs
4. **Log Integration**: Add Loki for log aggregation
5. **Custom Dashboards**: Create dashboards for specific use cases
6. **Load Testing**: Use k6/Locust to generate traffic
7. **Service Mesh**: Integrate with Istio for advanced routing

## 📖 Additional Resources

- [OpenTelemetry Docs](https://opentelemetry.io/docs/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [Prometheus Guides](https://prometheus.io/docs/introduction/overview/)
- [Grafana Tutorials](https://grafana.com/tutorials/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🤝 Contributing

This is a learning project demonstrating production observability patterns. Feel free to:
- Add new features
- Improve documentation
- Share your dashboards
- Report issues

## 📄 License

MIT License - feel free to use this for learning and production projects.

---

**Built with ❤️ to demonstrate real-world observability practices**

# Troubleshooting Guide

## Common Issues and Solutions

### 1. Backend Won't Start

#### Issue: Port 8080 already in use
```bash
# Check what's using port 8080
lsof -i :8080
# or
netstat -an | grep 8080

# Kill the process
kill -9 <PID>

# Or change backend port in application.properties
server.port=8081
```

#### Issue: Maven build fails
```bash
# Clean and rebuild
cd backend
mvn clean install

# Skip tests if they fail
mvn clean install -DskipTests

# Check Java version (needs 17+)
java -version
```

#### Issue: "Cannot connect to Jaeger"
```bash
# Check if Jaeger is running
docker ps | grep jaeger

# Check network connectivity
docker exec ai-backend ping jaeger

# Verify endpoint in application.properties
otel.exporter.otlp.endpoint=http://jaeger:4317
```

### 2. Frontend Issues

#### Issue: CORS errors in browser console
```
Access to fetch at 'http://localhost:8080/api/sentiment/analyze' from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Solution**: Verify backend CORS configuration
```properties
# In application.properties
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
```

#### Issue: OpenTelemetry errors in console
```
Failed to export traces to http://localhost:4318
```

**Solution**: 
1. Check if Jaeger HTTP endpoint is accessible:
```bash
curl http://localhost:4318/v1/traces
```

2. Update frontend tracing config if needed:
```javascript
const exporter = new OTLPTraceExporter({
  url: 'http://localhost:4318/v1/traces',
});
```

#### Issue: npm install fails
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and try again
rm -rf node_modules package-lock.json
npm install

# Use specific npm version
npm install -g npm@9
```

### 3. Docker Issues

#### Issue: Docker containers won't start
```bash
# Check Docker daemon
docker ps

# View logs
cd docker
docker-compose logs

# Restart Docker Desktop
# (macOS/Windows: Restart Docker Desktop app)
# (Linux: sudo systemctl restart docker)
```

#### Issue: Containers keep restarting
```bash
# Check specific container logs
docker-compose logs backend
docker-compose logs jaeger
docker-compose logs prometheus
docker-compose logs grafana

# Check resource limits
docker stats

# Increase Docker resources if needed
# (Docker Desktop -> Settings -> Resources)
```

#### Issue: Cannot remove containers
```bash
# Stop all containers
docker-compose down

# Force remove
docker-compose down -v --remove-orphans

# Nuclear option (removes ALL Docker data)
docker system prune -a --volumes
```

### 4. Observability Stack Issues

#### Issue: No traces in Jaeger

**Check 1**: Verify traces are being created
```bash
# Backend logs should show trace IDs
docker-compose logs backend | grep "traceId"
```

**Check 2**: Verify Jaeger is receiving traces
```bash
# Check Jaeger collector logs
docker-compose logs jaeger | grep "span"
```

**Check 3**: Try searching different ways
- In Jaeger UI, try "Find Traces" without filters
- Select "ai-sentiment-analyzer" service
- Increase lookback time (last hour/day)

**Check 4**: Verify OTLP endpoint
```bash
# Should return Jaeger's gRPC endpoint
docker exec ai-backend env | grep OTEL_EXPORTER_OTLP_ENDPOINT
```

#### Issue: No metrics in Prometheus

**Check 1**: Verify Actuator endpoint works
```bash
# Should return metrics in Prometheus format
curl http://localhost:8080/actuator/prometheus
```

**Check 2**: Check Prometheus targets
- Open: http://localhost:9090/targets
- Backend should be in state "UP"
- If "DOWN", check logs: `docker-compose logs prometheus`

**Check 3**: Verify Prometheus configuration
```bash
docker exec prometheus cat /etc/prometheus/prometheus.yml
# Should include:
# - job_name: 'spring-boot-app'
#   static_configs:
#     - targets: ['backend:8080']
```

**Check 4**: Test scraping manually
```bash
# From Prometheus container
docker exec prometheus wget -O- http://backend:8080/actuator/prometheus
```

#### Issue: Grafana dashboard shows "No Data"

**Check 1**: Verify Prometheus datasource
- Grafana -> Configuration -> Data Sources
- Should see "Prometheus" as default
- Click "Test" - should succeed

**Check 2**: Test query directly
- Open Prometheus: http://localhost:9090
- Run query: `up{job="spring-boot-app"}`
- Should return result

**Check 3**: Check time range
- In Grafana, verify time range (top right)
- Try "Last 30 minutes" or "Last 1 hour"

**Check 4**: Generate some traffic
```bash
# Make some requests to generate metrics
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/sentiment/analyze \
    -H "Content-Type: application/json" \
    -d '{"text":"Test message"}'
  sleep 1
done
```

### 5. AI API Issues

#### Issue: "Failed to analyze sentiment"

**Check 1**: Verify API key is set
```bash
docker exec ai-backend env | grep ANTHROPIC_API_KEY
# Should NOT be empty or "your-api-key-here"
```

**Check 2**: Update API key
```bash
# Edit docker/.env
echo "ANTHROPIC_API_KEY=sk-ant-your-actual-key" > docker/.env

# Restart backend
docker-compose restart backend
```

**Check 3**: Test API key manually
```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: YOUR_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  -d '{"model":"claude-sonnet-4-20250514","max_tokens":1024,"messages":[{"role":"user","content":"Hello"}]}'
```

**Check 4**: Check rate limits
- Anthropic API has rate limits
- Wait a minute and try again
- Check your usage at https://console.anthropic.com

### 6. Performance Issues

#### Issue: High memory usage
```bash
# Check memory usage
docker stats

# Increase JVM heap if needed (backend)
# Add to docker-compose.yml under backend service:
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m

# Restart
docker-compose restart backend
```

#### Issue: Slow response times
```bash
# Check where time is spent in Jaeger
# Look for spans with high duration

# Check backend logs for slow operations
docker-compose logs backend | grep "took"

# Verify external API (Claude) is responding quickly
curl -w "@curl-format.txt" -o /dev/null -s https://api.anthropic.com
```

## FAQ

### Q: How do I change the AI model?
**A**: Edit `backend/src/main/resources/application.properties`:
```properties
ai.api.model=claude-sonnet-4-20250514  # or claude-opus-4
```

### Q: How do I add more observability?
**A**: 
1. Add custom spans in your code
2. Add custom metrics with Micrometer
3. Add log statements with trace context
4. Create new Grafana dashboards

### Q: Can I deploy this to production?
**A**: Yes, but consider:
- Use persistent storage for Prometheus/Grafana
- Implement proper authentication
- Set up TLS/SSL
- Configure proper log rotation
- Use managed services (AWS X-Ray, Datadog, etc.)
- Implement sampling strategies
- Set up alerting

### Q: How do I add tracing to my own endpoints?
**A**: OpenTelemetry auto-instruments Spring Boot:
```java
@GetMapping("/my-endpoint")
public String myEndpoint() {
    // This is automatically traced!
    // To add custom attributes:
    Span.current().setAttribute("my.attribute", "value");
    return "result";
}
```

### Q: How do I create custom metrics?
**A**: Use Micrometer:
```java
@Service
public class MyService {
    private final Counter myCounter;
    
    public MyService(MeterRegistry registry) {
        myCounter = Counter.builder("my.custom.metric")
            .tag("type", "business")
            .register(registry);
    }
    
    public void doSomething() {
        myCounter.increment();
        // ...
    }
}
```

### Q: How do I correlate logs with traces?
**A**: Trace IDs are automatically added to logs:
```properties
# In application.properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg traceId=%X{traceId} spanId=%X{spanId}%n
```

Then in Grafana/Loki, you can click from a trace to its logs.

### Q: How much overhead does tracing add?
**A**: Minimal (< 5% typically):
- Most overhead is in network/serialization
- Sampling reduces overhead for high-volume systems
- BatchSpanProcessor minimizes network calls

### Q: Should I trace everything?
**A**: Start with 100% sampling in development:
```properties
otel.traces.sampler=always_on
```

In production, use probabilistic sampling:
```properties
otel.traces.sampler=traceidratio
otel.traces.sampler.arg=0.1  # 10% of traces
```

### Q: What if Jaeger/Prometheus goes down?
**A**: Your app continues working! Observability systems are designed to fail gracefully. Spans/metrics are dropped, but your app keeps serving requests.

### Q: How do I export data for long-term storage?
**A**:
- Prometheus: Configure remote write to services like Cortex, Thanos, or managed solutions
- Jaeger: Use Cassandra or Elasticsearch for storage
- Consider managed observability platforms

## Getting Help

If you're still stuck:

1. **Check Logs**
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs backend
docker-compose logs jaeger
```

2. **Check Service Health**
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Prometheus health
curl http://localhost:9090/-/healthy

# Grafana health
curl http://localhost:3001/api/health
```

3. **Reset Everything**
```bash
cd docker
docker-compose down -v
docker-compose up -d
```

4. **Resources**
- [OpenTelemetry Docs](https://opentelemetry.io/docs/)
- [Jaeger Docs](https://www.jaegertracing.io/docs/)
- [Prometheus Docs](https://prometheus.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## Success Checklist

✅ All Docker containers are running (`docker ps`)
✅ Backend health check passes (`curl http://localhost:8080/actuator/health`)
✅ Frontend loads (`http://localhost:3000`)
✅ Can analyze sentiment successfully
✅ Trace appears in Jaeger (`http://localhost:16686`)
✅ Metrics appear in Prometheus (`http://localhost:9090`)
✅ Grafana dashboard shows data (`http://localhost:3001`)

If all checks pass, you're good to go! 🎉

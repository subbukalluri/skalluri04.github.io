/**
 * OpenTelemetry Browser Tracing Configuration
 * 
 * This sets up distributed tracing in the browser.
 * It automatically instruments fetch API calls and XMLHttpRequests.
 * 
 * Key concepts:
 * - Every user action creates a trace
 * - API calls are automatically tracked
 * - Traces are sent to Jaeger via OTLP HTTP exporter
 * - Traces from frontend and backend are correlated by trace ID
 */

import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { SimpleSpanProcessor } from '@opentelemetry/sdk-trace-base';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request';

// Initialize OpenTelemetry
export function initTelemetry() {
  // Create a resource with service information
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'ai-sentiment-frontend',
    [SemanticResourceAttributes.SERVICE_VERSION]: '1.0.0',
  });

  // Create the tracer provider
  const provider = new WebTracerProvider({
    resource: resource,
  });

  // Configure the OTLP exporter to send traces to Jaeger
  // Note: In production, you'd send this through a collector or gateway
  const exporter = new OTLPTraceExporter({
    url: 'http://localhost:4318/v1/traces', // Jaeger OTLP HTTP endpoint
  });

  // Use SimpleSpanProcessor for immediate export (good for development)
  // In production, use BatchSpanProcessor for better performance
  provider.addSpanProcessor(new SimpleSpanProcessor(exporter));

  // Register the provider globally
  provider.register();

  // Auto-instrument fetch and XHR calls
  registerInstrumentations({
    instrumentations: [
      new FetchInstrumentation({
        // Propagate trace context to backend
        propagateTraceHeaderCorsUrls: [
          'http://localhost:8080',
        ],
        // Add custom attributes to spans
        applyCustomAttributesOnSpan: (span, request, response) => {
          span.setAttribute('http.url', request.url);
          if (response) {
            span.setAttribute('http.status_code', response.status);
          }
        },
      }),
      new XMLHttpRequestInstrumentation({
        propagateTraceHeaderCorsUrls: [
          'http://localhost:8080',
        ],
      }),
    ],
  });

  console.log('OpenTelemetry initialized for browser');
}

// Get the global tracer for manual span creation
export function getTracer() {
  const { trace } = require('@opentelemetry/api');
  return trace.getTracer('ai-sentiment-frontend', '1.0.0');
}

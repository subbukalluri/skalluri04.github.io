/**
 * Sentiment Analyzer Component
 * 
 * This is the main UI component that:
 * 1. Accepts user input
 * 2. Calls the backend API
 * 3. Displays results with trace information
 * 4. Creates manual spans for user interactions
 */

import React, { useState } from 'react';
import { getTracer } from '../tracing';
import sentimentService from '../services/sentimentService';
import './SentimentAnalyzer.css';

function SentimentAnalyzer() {
  const [text, setText] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Get the tracer for manual span creation
  const tracer = getTracer();

  const handleAnalyze = async () => {
    if (!text.trim()) {
      setError('Please enter some text to analyze');
      return;
    }

    // Create a manual span for the user action
    const span = tracer.startSpan('user.analyze_sentiment');
    span.setAttribute('input.text.length', text.length);

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // The API call is automatically instrumented by OpenTelemetry
      const response = await sentimentService.analyzeSentiment(text);
      
      span.setAttribute('result.sentiment', response.sentiment);
      span.setAttribute('result.confidence', response.confidence);
      span.setAttribute('backend.trace_id', response.traceId);
      
      setResult(response);
      span.setStatus({ code: 1 }); // OK status
      
      console.log('Analysis complete. Trace ID:', response.traceId);
    } catch (err) {
      setError('Failed to analyze sentiment. Please try again.');
      span.recordException(err);
      span.setStatus({ code: 2, message: err.message }); // ERROR status
      console.error('Analysis failed:', err);
    } finally {
      setLoading(false);
      span.end();
    }
  };

  const handleClear = () => {
    setText('');
    setResult(null);
    setError(null);
  };

  const getSentimentColor = (sentiment) => {
    switch (sentiment?.toLowerCase()) {
      case 'positive':
        return '#4caf50';
      case 'negative':
        return '#f44336';
      case 'neutral':
        return '#ff9800';
      default:
        return '#9e9e9e';
    }
  };

  const getSentimentEmoji = (sentiment) => {
    switch (sentiment?.toLowerCase()) {
      case 'positive':
        return '😊';
      case 'negative':
        return '😔';
      case 'neutral':
        return '😐';
      default:
        return '🤔';
    }
  };

  return (
    <div className="sentiment-analyzer">
      <div className="container">
        <header className="header">
          <h1>🤖 AI Sentiment Analyzer</h1>
          <p className="subtitle">
            Powered by Claude AI with Full Observability Stack
          </p>
        </header>

        <div className="input-section">
          <textarea
            className="text-input"
            placeholder="Enter text to analyze sentiment... 
            
Example: 'I absolutely love this product! It exceeded all my expectations.'"
            value={text}
            onChange={(e) => setText(e.target.value)}
            rows={6}
          />
          
          <div className="button-group">
            <button
              className="btn btn-primary"
              onClick={handleAnalyze}
              disabled={loading || !text.trim()}
            >
              {loading ? 'Analyzing...' : 'Analyze Sentiment'}
            </button>
            <button
              className="btn btn-secondary"
              onClick={handleClear}
              disabled={loading}
            >
              Clear
            </button>
          </div>
        </div>

        {error && (
          <div className="error-message">
            ⚠️ {error}
          </div>
        )}

        {result && (
          <div className="result-section">
            <div 
              className="sentiment-card"
              style={{ borderColor: getSentimentColor(result.sentiment) }}
            >
              <div className="sentiment-header">
                <span className="sentiment-emoji">
                  {getSentimentEmoji(result.sentiment)}
                </span>
                <h2 
                  className="sentiment-label"
                  style={{ color: getSentimentColor(result.sentiment) }}
                >
                  {result.sentiment?.toUpperCase()}
                </h2>
              </div>

              <div className="confidence-bar">
                <div className="confidence-label">
                  Confidence: {(result.confidence * 100).toFixed(1)}%
                </div>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${result.confidence * 100}%`,
                      backgroundColor: getSentimentColor(result.sentiment),
                    }}
                  />
                </div>
              </div>

              <div className="analysis-text">
                <h3>Analysis</h3>
                <p>{result.analysis}</p>
              </div>

              <div className="metrics">
                <div className="metric">
                  <span className="metric-label">Processing Time</span>
                  <span className="metric-value">{result.processingTimeMs}ms</span>
                </div>
                <div className="metric">
                  <span className="metric-label">Trace ID</span>
                  <span className="metric-value trace-id" title={result.traceId}>
                    {result.traceId?.substring(0, 16)}...
                  </span>
                </div>
              </div>

              <div className="observability-note">
                <span className="info-icon">ℹ️</span>
                <span>
                  This request is fully traced! Check Jaeger UI to see the complete trace 
                  from frontend → backend → AI API
                </span>
              </div>
            </div>
          </div>
        )}

        <div className="info-panel">
          <h3>📊 Observability Features</h3>
          <ul>
            <li><strong>Distributed Tracing:</strong> View complete request flow in Jaeger</li>
            <li><strong>Metrics:</strong> Monitor performance in Grafana dashboards</li>
            <li><strong>Logs:</strong> Correlated logs with trace IDs</li>
            <li><strong>Real-time:</strong> All telemetry data updates in real-time</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default SentimentAnalyzer;

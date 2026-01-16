/**
 * Main App Component
 * 
 * Initializes OpenTelemetry and renders the application
 */

import React, { useEffect } from 'react';
import { initTelemetry } from './tracing';
import SentimentAnalyzer from './components/SentimentAnalyzer';
import './App.css';

function App() {
  useEffect(() => {
    // Initialize OpenTelemetry when the app starts
    initTelemetry();
    console.log('AI Sentiment Analyzer started with full observability');
  }, []);

  return (
    <div className="App">
      <SentimentAnalyzer />
    </div>
  );
}

export default App;

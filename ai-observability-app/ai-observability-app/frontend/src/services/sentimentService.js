/**
 * Sentiment Analysis Service
 * 
 * This service handles communication with the backend API.
 * All API calls are automatically traced by OpenTelemetry's fetch instrumentation.
 */

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/sentiment';

class SentimentService {
  /**
   * Analyze sentiment of text
   * 
   * @param {string} text - Text to analyze
   * @returns {Promise} Response with sentiment analysis
   */
  async analyzeSentiment(text) {
    try {
      // This fetch call is automatically instrumented by OpenTelemetry
      const response = await fetch(`${API_BASE_URL}/analyze`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ text }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error analyzing sentiment:', error);
      throw error;
    }
  }

  /**
   * Check backend health
   */
  async checkHealth() {
    try {
      const response = await fetch(`${API_BASE_URL}/health`);
      return await response.json();
    } catch (error) {
      console.error('Health check failed:', error);
      return { status: 'DOWN' };
    }
  }
}

export default new SentimentService();

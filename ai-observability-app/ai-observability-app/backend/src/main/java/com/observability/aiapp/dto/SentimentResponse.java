package com.observability.aiapp.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SentimentResponse {
    private String sentiment;
    private Double confidence;
    private String analysis;
    private Long processingTimeMs;
    private String traceId;
}

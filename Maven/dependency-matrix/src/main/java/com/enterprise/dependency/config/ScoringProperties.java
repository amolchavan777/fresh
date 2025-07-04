package com.enterprise.dependency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for claim scoring rules and data quality penalties.
 * Allows tuning of weights and thresholds without code changes.
 */
@Configuration
@ConfigurationProperties(prefix = "scoring")
public class ScoringProperties {
    /** Default score if no source type match. */
    private double defaultScore = 0.5;
    /** Per-source base scores. */
    private Map<String, Double> sourceBaseScore = new HashMap<>();
    /** Penalty for short or missing processedData. */
    private double processedDataPenalty = 0.2;
    /** Minimum processedData length for no penalty. */
    private int minProcessedDataLength = 5;
    /** Minimum and maximum allowed score. */
    private double minScore = 0.0;
    private double maxScore = 1.0;
    /** Per-field penalties (e.g., missing id, short processedData, old timestamp, etc.) */
    private Map<String, Double> fieldPenalties = new HashMap<>();
    /** Max claim age in days before penalty applies. */
    private int maxClaimAgeDays = 30;
    /** Penalty for claim older than maxClaimAgeDays. */
    private double oldClaimPenalty = 0.1;

    public double getDefaultScore() { return defaultScore; }
    public void setDefaultScore(double defaultScore) { this.defaultScore = defaultScore; }
    public Map<String, Double> getSourceBaseScore() { return sourceBaseScore; }
    public void setSourceBaseScore(Map<String, Double> sourceBaseScore) { this.sourceBaseScore = sourceBaseScore; }
    public double getProcessedDataPenalty() { return processedDataPenalty; }
    public void setProcessedDataPenalty(double processedDataPenalty) { this.processedDataPenalty = processedDataPenalty; }
    public int getMinProcessedDataLength() { return minProcessedDataLength; }
    public void setMinProcessedDataLength(int minProcessedDataLength) { this.minProcessedDataLength = minProcessedDataLength; }
    public double getMinScore() { return minScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
    public Map<String, Double> getFieldPenalties() { return fieldPenalties; }
    public void setFieldPenalties(Map<String, Double> fieldPenalties) { this.fieldPenalties = fieldPenalties; }
    public int getMaxClaimAgeDays() { return maxClaimAgeDays; }
    public void setMaxClaimAgeDays(int maxClaimAgeDays) { this.maxClaimAgeDays = maxClaimAgeDays; }
    public double getOldClaimPenalty() { return oldClaimPenalty; }
    public void setOldClaimPenalty(double oldClaimPenalty) { this.oldClaimPenalty = oldClaimPenalty; }
}

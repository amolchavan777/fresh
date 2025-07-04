package com.enterprise.dependency.model;

/**
 * Represents a confidence score for a dependency claim.
 * <p>
 * Example usage:
 * <pre>
 *   ConfidenceScore score = new ConfidenceScore(0.9f);
 * </pre>
 */
public class ConfidenceScore {
    private float value;

    public ConfidenceScore(float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException("Confidence score must be between 0 and 1");
        }
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException("Confidence score must be between 0 and 1");
        }
        this.value = value;
    }
}

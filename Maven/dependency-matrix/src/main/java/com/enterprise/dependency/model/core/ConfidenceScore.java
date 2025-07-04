package com.enterprise.dependency.model.core;

import java.util.Objects;

/**
 * Represents a confidence score for a dependency claim.
 * <p>
 * Example:
 * <pre>
 *   ConfidenceScore score = ConfidenceScore.of(0.95);
 * </pre>
 */
public final class ConfidenceScore {
    private final double value;

    private ConfidenceScore(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Confidence score must be between 0.0 and 1.0");
        }
        this.value = value;
    }

    public static ConfidenceScore of(double value) {
        return new ConfidenceScore(value);
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfidenceScore that = (ConfidenceScore) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("%.2f", value);
    }
}

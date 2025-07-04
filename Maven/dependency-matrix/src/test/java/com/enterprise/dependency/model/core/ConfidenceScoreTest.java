package com.enterprise.dependency.model.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConfidenceScore}.
 */
class ConfidenceScoreTest {
    @Test
    void ofShouldCreateValidScore() {
        ConfidenceScore score = ConfidenceScore.of(0.8);
        assertEquals(0.8, score.getValue(), 0.0001);
    }

    @Test
    void ofShouldThrowOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> ConfidenceScore.of(-0.1));
        assertThrows(IllegalArgumentException.class, () -> ConfidenceScore.of(1.1));
    }
}

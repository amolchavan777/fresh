package com.enterprise.dependency.model.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Dependency}.
 */
class DependencyTest {
    @Test
    void builderShouldCreateValidDependency() {
        Dependency dep = Dependency.builder()
                .sourceAppId("app-001")
                .targetAppId("app-002")
                .type(DependencyType.RUNTIME)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build();
        assertEquals("app-001", dep.getSourceAppId());
        assertEquals("app-002", dep.getTargetAppId());
        assertEquals(DependencyType.RUNTIME, dep.getType());
        assertEquals(0.95, dep.getConfidenceScore().getValue(), 0.0001);
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            Dependency.builder().build()
        );
        assertTrue(ex.getMessage().contains("sourceAppId"));
    }
}

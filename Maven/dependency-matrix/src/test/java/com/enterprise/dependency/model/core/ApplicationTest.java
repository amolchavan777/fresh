package com.enterprise.dependency.model.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Application}.
 */
class ApplicationTest {
    @Test
    void builderShouldCreateValidApplication() {
        Application app = Application.builder()
                .id("app-001")
                .name("User Service")
                .type("microservice")
                .environment("prod")
                .owner("team-a")
                .build();
        assertEquals("app-001", app.getId());
        assertEquals("User Service", app.getName());
        assertEquals("microservice", app.getType());
        assertEquals("prod", app.getEnvironment());
        assertEquals("team-a", app.getOwner());
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            Application.builder().build()
        );
        assertTrue(ex.getMessage().contains("id"));
    }
}

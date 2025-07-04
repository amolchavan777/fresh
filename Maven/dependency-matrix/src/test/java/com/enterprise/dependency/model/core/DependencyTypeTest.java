package com.enterprise.dependency.model.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DependencyType}.
 */
class DependencyTypeTest {
    @Test
    void enumShouldContainExpectedValues() {
        assertNotNull(DependencyType.valueOf("RUNTIME"));
        assertNotNull(DependencyType.valueOf("BUILD"));
        assertNotNull(DependencyType.valueOf("API"));
        assertNotNull(DependencyType.valueOf("DATABASE"));
        assertNotNull(DependencyType.valueOf("NETWORK"));
        assertNotNull(DependencyType.valueOf("OTHER"));
    }
}

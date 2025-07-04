package com.enterprise.dependency.model.sources;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CodebaseDependency}.
 */
class CodebaseDependencyTest {
    @Test
    void builderShouldCreateValidDependency() {
        CodebaseDependency dep = CodebaseDependency.builder()
                .groupId("com.enterprise")
                .artifactId("user-service-client")
                .version("2.1.0")
                .rawXml("<dependency>...</dependency>")
                .build();
        assertEquals("com.enterprise", dep.getGroupId());
        assertEquals("user-service-client", dep.getArtifactId());
        assertEquals("2.1.0", dep.getVersion());
        assertEquals("<dependency>...</dependency>", dep.getRawXml());
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            CodebaseDependency.builder().build()
        );
        assertTrue(ex.getMessage().contains("groupId"));
    }
}

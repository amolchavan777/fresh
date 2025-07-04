package com.enterprise.dependency.model.sources;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RouterLogEntry}.
 */
class RouterLogEntryTest {
    @Test
    void builderShouldCreateValidEntry() {
        RouterLogEntry entry = RouterLogEntry.builder()
                .timestamp(LocalDateTime.now())
                .sourceIp("192.168.1.100")
                .targetIp("192.168.1.200")
                .targetPort(8080)
                .protocol("HTTP")
                .method("GET")
                .endpoint("/api/users")
                .statusCode(200)
                .responseTimeMs(125)
                .rawLine("2024-07-04 10:30:45 [INFO] ...")
                .build();
        assertEquals("192.168.1.100", entry.getSourceIp());
        assertEquals("192.168.1.200", entry.getTargetIp());
        assertEquals(8080, entry.getTargetPort());
        assertEquals("HTTP", entry.getProtocol());
        assertEquals("GET", entry.getMethod());
        assertEquals("/api/users", entry.getEndpoint());
        assertEquals(200, entry.getStatusCode());
        assertEquals(125, entry.getResponseTimeMs());
        assertNotNull(entry.getTimestamp());
        assertNotNull(entry.getRawLine());
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            RouterLogEntry.builder().build()
        );
        assertTrue(ex.getMessage().contains("timestamp"));
    }
}

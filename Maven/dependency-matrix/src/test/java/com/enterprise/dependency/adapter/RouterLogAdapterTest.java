package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.sources.RouterLogEntry;
import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RouterLogAdapter}.
 */
class RouterLogAdapterTest {
    private final RouterLogAdapter adapter = new RouterLogAdapter();

    @Test
    void parseLogLineShouldReturnEntryForValidLine() {
        String line = "2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 HTTP GET /api/users 200 125ms";
        RouterLogEntry entry = adapter.parseLogLine(line);
        assertNotNull(entry);
        assertEquals("192.168.1.100", entry.getSourceIp());
        assertEquals("192.168.1.200", entry.getTargetIp());
        assertEquals(8080, entry.getTargetPort());
        assertEquals("HTTP", entry.getProtocol());
        assertEquals("GET", entry.getMethod());
        assertEquals("/api/users", entry.getEndpoint());
        assertEquals(200, entry.getStatusCode());
        assertEquals(125, entry.getResponseTimeMs());
    }

    @Test
    void parseLogLineShouldReturnNullForInvalidLine() {
        String line = "invalid log line";
        RouterLogEntry entry = adapter.parseLogLine(line);
        assertNull(entry);
    }

    @Test
    void toClaimShouldReturnValidClaim() {
        String line = "2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 HTTP GET /api/users 200 125ms";
        RouterLogEntry entry = adapter.parseLogLine(line);
        Claim claim = adapter.toClaim(entry, line);
        assertNotNull(claim);
        assertEquals("ROUTER_LOG", claim.getSourceType());
        assertTrue(claim.getProcessedData().contains("192.168.1.100"));
        assertTrue(claim.getProcessedData().contains("192.168.1.200"));
    }

    @Test
    void parseLogFileShouldReturnClaimsForValidFile() throws Exception {
        Path tempFile = Files.createTempFile("router", ".log");
        List<String> lines = List.of(
            "2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 HTTP GET /api/users 200 125ms",
            "2024-07-04 10:30:46 [INFO] 192.168.1.200 -> 192.168.1.150:3306 TCP connection established"
        );
        Files.write(tempFile, lines);
        List<Claim> claims = adapter.parseLogFile(tempFile);
        assertEquals(2, claims.size());
        Files.deleteIfExists(tempFile);
    }
}

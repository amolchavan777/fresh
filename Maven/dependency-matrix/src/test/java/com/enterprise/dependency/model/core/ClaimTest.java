package com.enterprise.dependency.model.core;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Claim}.
 */
class ClaimTest {
    @Test
    void builderShouldCreateValidClaim() {
        Claim claim = Claim.builder()
                .id("claim-001")
                .sourceType("ROUTER_LOG")
                .rawData("2024-07-04 10:30:45 ...")
                .processedData("app-001 -> app-002")
                .timestamp(Instant.now())
                .build();
        assertEquals("claim-001", claim.getId());
        assertEquals("ROUTER_LOG", claim.getSourceType());
        assertEquals("2024-07-04 10:30:45 ...", claim.getRawData());
        assertEquals("app-001 -> app-002", claim.getProcessedData());
        assertNotNull(claim.getTimestamp());
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            Claim.builder().build()
        );
        assertTrue(ex.getMessage().contains("id"));
    }
}

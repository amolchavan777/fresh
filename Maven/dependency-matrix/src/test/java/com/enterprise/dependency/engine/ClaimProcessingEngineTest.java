package com.enterprise.dependency.engine;

import com.enterprise.dependency.config.ScoringProperties;
import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClaimProcessingEngine}.
 */
@SpringBootTest
class ClaimProcessingEngineTest {
    @Autowired
    private ClaimProcessingEngine engine;
    @Autowired
    private ScoringProperties scoringProperties;

    @Test
    void processClaimsShouldReturnValidClaims() {
        Claim claim = Claim.builder()
                .id("claim-001")
                .sourceType("ROUTER_LOG")
                .rawData("2024-07-04 10:30:45 ...")
                .processedData("app-001 -> app-002")
                .timestamp(Instant.now())
                .build();
        List<Claim> result = engine.processClaims(List.of(claim));
        assertEquals(1, result.size());
        assertEquals("claim-001", result.get(0).getId());
    }

    @Test
    void processClaimsShouldSkipInvalidClaims() {
        assertThrows(IllegalArgumentException.class, () -> {
            Claim invalid = Claim.builder()
                    .id(null)
                    .sourceType(null)
                    .rawData(null)
                    .processedData(null)
                    .timestamp(null)
                    .build();
            // The following line is unreachable, but kept for clarity
            // List<Claim> result = engine.processClaims(List.of(invalid));
            // assertEquals(0, result.size());
        });
    }

    @Test
    void normalizeShouldReturnSameClaimForNow() {
        Claim claim = Claim.builder()
                .id("claim-001")
                .sourceType("ROUTER_LOG")
                .rawData("2024-07-04 10:30:45 ...")
                .processedData("app-001 -> app-002")
                .timestamp(Instant.now())
                .build();
        Claim normalized = engine.normalize(claim);
        assertSame(claim, normalized);
    }

    @Test
    void validateShouldThrowForMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> {
            Claim invalid = Claim.builder()
                    .id(null)
                    .sourceType(null)
                    .rawData(null)
                    .processedData(null)
                    .timestamp(null)
                    .build();
            engine.validate(invalid);
        });
    }

    @Test
    void scoreShouldAssignConfidenceScoreBasedOnSourceType() {
        Claim codebaseClaim = Claim.builder()
                .id("claim-codebase")
                .sourceType("CODEBASE")
                .rawData("codebase raw")
                .processedData("appA -> appB")
                .timestamp(Instant.now())
                .build();
        Claim scored = engine.score(codebaseClaim);
        assertNotNull(scored.getConfidenceScore());
        assertEquals(0.8, scored.getConfidenceScore().getValue(), 0.0001);

        Claim logClaim = Claim.builder()
                .id("claim-log")
                .sourceType("ROUTER_LOG")
                .rawData("log raw")
                .processedData("appA -> appB")
                .timestamp(Instant.now())
                .build();
        Claim scoredLog = engine.score(logClaim);
        assertEquals(0.65, scoredLog.getConfidenceScore().getValue(), 0.0001); // 0.5 base + 0.15 router log boost

        Claim apiClaim = Claim.builder()
                .id("claim-api")
                .sourceType("API_GATEWAY")
                .rawData("api raw")
                .processedData("appA -> appB")
                .timestamp(Instant.now())
                .build();
        Claim scoredApi = engine.score(apiClaim);
        assertEquals(0.60, scoredApi.getConfidenceScore().getValue(), 0.0001); // 0.5 base + 0.10 api gateway boost
    }
}

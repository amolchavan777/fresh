package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConflictResolutionEngine}.
 */
class ConflictResolutionEngineTest {
    private ConflictResolutionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ConflictResolutionEngine();
    }

    @Test
    void resolveClaimsShouldReturnEmptyListForNullInput() {
        List<Claim> result = engine.resolveClaims(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveClaimsShouldReturnEmptyListForEmptyInput() {
        List<Claim> result = engine.resolveClaims(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveClaimsShouldReturnSameClaimForSingleInput() {
        Claim claim = Claim.builder()
                .id("claim-001")
                .sourceType("CODEBASE")
                .rawData("dependency info")
                .processedData("app-a -> app-b")
                .timestamp(Instant.now())
                .confidenceScore(ConfidenceScore.of(0.95))
                .build();

        List<Claim> result = engine.resolveClaims(Arrays.asList(claim));
        assertEquals(1, result.size());
        assertEquals(claim, result.get(0));
    }

    @Test
    void resolveClaimsShouldPreferCodebaseOverRouterLog() {
        Instant now = Instant.now();
        
        Claim codebaseClaim = Claim.builder()
                .id("claim-codebase")
                .sourceType("CODEBASE")
                .rawData("maven dependency")
                .processedData("user-service -> database")
                .timestamp(now)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build();

        Claim routerClaim = Claim.builder()
                .id("claim-router")
                .sourceType("ROUTER_LOG")
                .rawData("HTTP request log")
                .processedData("user-service -> database")
                .timestamp(now)
                .confidenceScore(ConfidenceScore.of(0.85))
                .build();

        List<Claim> result = engine.resolveClaims(Arrays.asList(codebaseClaim, routerClaim));
        assertEquals(1, result.size());
        
        Claim resolved = result.get(0);
        assertEquals("CONFLICT_RESOLVED", resolved.getSourceType());
        assertTrue(resolved.getId().contains("codebase")); // Should be based on codebase claim
        assertEquals("user-service -> database", resolved.getProcessedData());
    }

    @Test
    void resolveClaimsShouldPreferRecentClaimsOverOldClaims() {
        Instant recentTime = Instant.now();
        Instant oldTime = recentTime.minus(48, ChronoUnit.HOURS); // 2 days old
        
        Claim recentClaim = Claim.builder()
                .id("claim-recent")
                .sourceType("ROUTER_LOG")
                .rawData("recent log")
                .processedData("app-x -> app-y")
                .timestamp(recentTime)
                .confidenceScore(ConfidenceScore.of(0.80))
                .build();

        Claim oldClaim = Claim.builder()
                .id("claim-old")
                .sourceType("ROUTER_LOG")
                .rawData("old log")
                .processedData("app-x -> app-y")
                .timestamp(oldTime)
                .confidenceScore(ConfidenceScore.of(0.85))
                .build();

        List<Claim> result = engine.resolveClaims(Arrays.asList(recentClaim, oldClaim));
        assertEquals(1, result.size());
        
        Claim resolved = result.get(0);
        assertTrue(resolved.getId().contains("recent")); // Should prefer recent claim
    }

    @Test
    void resolveClaimsShouldHandleMultipleDifferentDependencies() {
        Claim claim1 = Claim.builder()
                .id("claim-001")
                .sourceType("CODEBASE")
                .rawData("dep1")
                .processedData("app-a -> app-b")
                .timestamp(Instant.now())
                .confidenceScore(ConfidenceScore.of(0.95))
                .build();

        Claim claim2 = Claim.builder()
                .id("claim-002")
                .sourceType("ROUTER_LOG")
                .rawData("dep2")
                .processedData("app-c -> app-d")
                .timestamp(Instant.now())
                .confidenceScore(ConfidenceScore.of(0.85))
                .build();

        List<Claim> result = engine.resolveClaims(Arrays.asList(claim1, claim2));
        assertEquals(2, result.size()); // Should return both as they're different dependencies
    }

    @Test
    void applyBusinessRulesShouldReturnSameClaimForNow() {
        Claim claim = Claim.builder()
                .id("claim-001")
                .sourceType("CODEBASE")
                .rawData("business rule test")
                .processedData("app-a -> app-b")
                .timestamp(Instant.now())
                .confidenceScore(ConfidenceScore.of(0.95))
                .build();

        Claim result = engine.applyBusinessRules(claim);
        assertEquals(claim, result); // Business rules not implemented yet, should return same claim
    }

    @Test
    void resolveClaimsShouldHandleNullConfidenceScore() {
        Claim claimWithoutScore = Claim.builder()
                .id("claim-no-score")
                .sourceType("ROUTER_LOG")
                .rawData("no score")
                .processedData("app-a -> app-b")
                .timestamp(Instant.now())
                .build(); // No confidence score set

        List<Claim> result = engine.resolveClaims(Arrays.asList(claimWithoutScore));
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getConfidenceScore());
    }
}

package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.model.core.Dependency;
import com.enterprise.dependency.model.core.DependencyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InferenceEngine.
 */
class InferenceEngineTest {

    private InferenceEngine inferenceEngine;

    @BeforeEach
    void setUp() {
        inferenceEngine = new InferenceEngine();
    }

    @Test
    void shouldReturnEmptyListForNullClaims() {
        List<Dependency> result = inferenceEngine.inferDependencies(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyClaims() {
        List<Dependency> result = inferenceEngine.inferDependencies(Collections.emptyList());
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldInferSimpleDependency() {
        Claim claim = Claim.builder()
            .id("test-claim-1")
            .sourceType("ROUTER_LOG")
            .rawData("GET /api/users HTTP/1.1")
            .processedData("user-service -> users-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.8))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals("user-service", dependency.getSourceAppId());
        assertEquals("users-service", dependency.getTargetAppId());
        assertNotNull(dependency.getType());
        assertNotNull(dependency.getConfidenceScore());
        assertTrue(dependency.getConfidenceScore().getValue() > 0.0);
    }

    @Test
    void shouldInferDatabaseDependency() {
        Claim claim = Claim.builder()
            .id("test-claim-db")
            .sourceType("CODEBASE")
            .rawData("jdbc:postgresql://localhost:5432/userdb")
            .processedData("user-service -> user-database")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.95))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals(DependencyType.DATABASE, dependency.getType());
        assertEquals("user-service", dependency.getSourceAppId());
        assertEquals("user-database", dependency.getTargetAppId());
    }

    @Test
    void shouldInferApiDependency() {
        Claim claim = Claim.builder()
            .id("test-claim-api")
            .sourceType("API_GATEWAY")
            .rawData("GET /api/v1/users HTTP/1.1 200")
            .processedData("mobile-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.85))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals(DependencyType.API, dependency.getType());
        assertEquals("mobile-app", dependency.getSourceAppId());
        assertEquals("user-service", dependency.getTargetAppId());
    }

    @Test
    void shouldInferBuildDependency() {
        Claim claim = Claim.builder()
            .id("test-claim-build")
            .sourceType("CODEBASE")
            .rawData("com.enterprise:user-service-client:2.1.0")
            .processedData("order-service -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.95))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals(DependencyType.BUILD, dependency.getType());
        assertEquals("order-service", dependency.getSourceAppId());
        assertEquals("user-service", dependency.getTargetAppId());
    }

    @Test
    void shouldHandleMultipleClaimsForSameDependency() {
        Claim claim1 = Claim.builder()
            .id("test-claim-1")
            .sourceType("ROUTER_LOG")
            .rawData("GET /api/users HTTP/1.1")
            .processedData("mobile-app -> user-service")
            .timestamp(Instant.now().minusSeconds(3600))
            .confidenceScore(ConfidenceScore.of(0.7))
            .build();

        Claim claim2 = Claim.builder()
            .id("test-claim-2")
            .sourceType("API_GATEWAY")
            .rawData("GET /api/v1/users HTTP/1.1 200")
            .processedData("mobile-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.9))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim1, claim2));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals("mobile-app", dependency.getSourceAppId());
        assertEquals("user-service", dependency.getTargetAppId());
        // Should have higher confidence due to multiple supporting claims
        assertTrue(dependency.getConfidenceScore().getValue() > 0.7);
    }

    @Test
    void shouldInferMultipleDifferentDependencies() {
        Claim claim1 = Claim.builder()
            .id("test-claim-1")
            .sourceType("ROUTER_LOG")
            .rawData("GET /api/users HTTP/1.1")
            .processedData("mobile-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.8))
            .build();

        Claim claim2 = Claim.builder()
            .id("test-claim-2")
            .sourceType("ROUTER_LOG")
            .rawData("GET /api/orders HTTP/1.1")
            .processedData("mobile-app -> order-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.8))
            .build();

        Claim claim3 = Claim.builder()
            .id("test-claim-3")
            .sourceType("CODEBASE")
            .rawData("jdbc:postgresql://localhost:5432/orderdb")
            .processedData("order-service -> order-database")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.95))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim1, claim2, claim3));

        assertEquals(3, result.size());
        
        // Check that we have different source-target combinations
        List<String> dependencyKeys = result.stream()
            .map(dep -> dep.getSourceAppId() + " -> " + dep.getTargetAppId())
            .sorted()
            .toList();
        
        assertTrue(dependencyKeys.contains("mobile-app -> user-service"));
        assertTrue(dependencyKeys.contains("mobile-app -> order-service"));
        assertTrue(dependencyKeys.contains("order-service -> order-database"));
    }

    @Test
    void shouldFilterLowConfidenceDependencies() {
        Claim lowConfidenceClaim = Claim.builder()
            .id("test-claim-low")
            .sourceType("UNKNOWN")
            .rawData("some unclear data")
            .processedData("unknown-app -> unknown-service")
            .timestamp(Instant.now().minusSeconds(86400 * 7)) // Old data
            .confidenceScore(ConfidenceScore.of(0.1)) // Very low confidence
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(lowConfidenceClaim));

        // Should filter out low confidence dependencies
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleClaimsWithoutStandardFormat() {
        Claim malformedClaim = Claim.builder()
            .id("test-claim-malformed")
            .sourceType("ROUTER_LOG")
            .rawData("some malformed data")
            .processedData("malformed data without arrow")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.8))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(malformedClaim));

        // Should handle gracefully - may or may not create dependency based on implementation
        assertNotNull(result);
    }

    @Test
    void shouldDeduplicateDependencies() {
        // Create two different claims that result in the same dependency but with different confidence
        Claim claim1 = Claim.builder()
            .id("test-claim-1")
            .sourceType("ROUTER_LOG")
            .rawData("GET /api/users HTTP/1.1")
            .processedData("web-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.7))
            .build();

        Claim claim2 = Claim.builder()
            .id("test-claim-2")
            .sourceType("API_GATEWAY")
            .rawData("GET /api/v1/users HTTP/1.1 200")
            .processedData("web-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.9))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(claim1, claim2));

        // Should be deduplicated to single dependency
        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        assertEquals("web-app", dependency.getSourceAppId());
        assertEquals("user-service", dependency.getTargetAppId());
        // Should have higher confidence from the combined analysis
        assertTrue(dependency.getConfidenceScore().getValue() >= 0.7);
    }

    @Test
    void shouldCalculateConfidenceBasedOnMultipleFactors() {
        // Recent, high-confidence claim from reliable source
        Claim recentHighConfidenceClaim = Claim.builder()
            .id("test-claim-recent")
            .sourceType("CODEBASE") // High priority source
            .rawData("com.enterprise:user-service-client:2.1.0")
            .processedData("web-app -> user-service")
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.95))
            .build();

        // Old, lower confidence claim from less reliable source
        Claim oldLowConfidenceClaim = Claim.builder()
            .id("test-claim-old")
            .sourceType("NETWORK") // Lower priority source
            .rawData("TCP connection to user-service:8080")
            .processedData("web-app -> user-service")
            .timestamp(Instant.now().minusSeconds(86400 * 7)) // Week old
            .confidenceScore(ConfidenceScore.of(0.6))
            .build();

        List<Dependency> result = inferenceEngine.inferDependencies(Arrays.asList(recentHighConfidenceClaim, oldLowConfidenceClaim));

        assertEquals(1, result.size());
        
        Dependency dependency = result.get(0);
        // Should have high confidence due to recent, high-confidence codebase source
        assertTrue(dependency.getConfidenceScore().getValue() > 0.7);
    }
}

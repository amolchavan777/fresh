package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ConflictResolutionEngine implements weighted voting algorithms to resolve
 * contradictory dependency claims from multiple sources.
 * 
 * <p>Resolution strategies include:
 * <ul>
 *   <li>Source priority weighting (Codebase > Router Logs > API Gateway, etc.)</li>
 *   <li>Recency weighting (newer data gets higher weight)</li>
 *   <li>Frequency weighting (frequently observed dependencies get higher confidence)</li>
 *   <li>Business rule application for manual overrides</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   ConflictResolutionEngine resolver = new ConflictResolutionEngine();
 *   List&lt;Claim&gt; conflictingClaims = Arrays.asList(claim1, claim2, claim3);
 *   Claim resolvedClaim = resolver.resolveClaims(conflictingClaims);
 * </pre>
 */
@Service
public class ConflictResolutionEngine {
    private static final Logger logger = LoggerFactory.getLogger(ConflictResolutionEngine.class);
    
    // Source priority weights per PRD requirements
    private static final Map<String, Double> SOURCE_PRIORITIES = Map.of(
        "CODEBASE", 0.95,
        "ROUTER_LOG", 0.85,
        "API_GATEWAY", 0.80,
        "CI_CD", 0.75,
        "TELEMETRY", 0.70,
        "NETWORK", 0.60
    );
    
    // Recency decay factor (claims older than this get reduced weight)
    private static final long RECENCY_THRESHOLD_HOURS = 24;
    private static final double RECENCY_DECAY_FACTOR = 0.8;

    /**
     * Resolves conflicting claims using weighted voting algorithm.
     * 
     * @param claims List of potentially conflicting claims
     * @return Single resolved claim with highest weighted confidence
     */
    public List<Claim> resolveClaims(List<Claim> claims) {
        if (claims == null || claims.isEmpty()) {
            logger.warn("No claims provided for conflict resolution");
            return Collections.emptyList();
        }
        
        if (claims.size() == 1) {
            logger.debug("Single claim provided, no conflict resolution needed");
            Claim singleClaim = claims.get(0);
            // Ensure single claims have a confidence score
            if (singleClaim.getConfidenceScore() == null) {
                singleClaim = Claim.builder()
                    .id(singleClaim.getId())
                    .sourceType(singleClaim.getSourceType())
                    .rawData(singleClaim.getRawData())
                    .processedData(singleClaim.getProcessedData())
                    .timestamp(singleClaim.getTimestamp())
                    .confidenceScore(ConfidenceScore.of(0.5)) // Default confidence
                    .build();
            }
            return Arrays.asList(singleClaim);
        }
        
        logger.info("Resolving conflicts for {} claims", claims.size());
        
        // Group claims by dependency relationship (source -> target)
        Map<String, List<Claim>> claimGroups = groupClaimsByDependency(claims);
        
        List<Claim> resolvedClaims = new ArrayList<>();
        
        for (Map.Entry<String, List<Claim>> entry : claimGroups.entrySet()) {
            String dependencyKey = entry.getKey();
            List<Claim> conflictingClaims = entry.getValue();
            
            if (conflictingClaims.size() == 1) {
                Claim singleClaim = conflictingClaims.get(0);
                // Ensure single claims have a confidence score
                if (singleClaim.getConfidenceScore() == null) {
                    singleClaim = Claim.builder()
                        .id(singleClaim.getId())
                        .sourceType(singleClaim.getSourceType())
                        .rawData(singleClaim.getRawData())
                        .processedData(singleClaim.getProcessedData())
                        .timestamp(singleClaim.getTimestamp())
                        .confidenceScore(ConfidenceScore.of(0.5)) // Default confidence
                        .build();
                }
                resolvedClaims.add(singleClaim);
            } else {
                logger.debug("Resolving {} conflicting claims for dependency: {}", 
                    conflictingClaims.size(), dependencyKey);
                Claim resolved = resolveConflictingClaims(conflictingClaims);
                resolvedClaims.add(resolved);
            }
        }
        
        logger.info("Resolved {} claim groups into {} final claims", 
            claimGroups.size(), resolvedClaims.size());
        
        return resolvedClaims;
    }
    
    /**
     * Groups claims by their dependency relationship (source app -> target app).
     */
    private Map<String, List<Claim>> groupClaimsByDependency(List<Claim> claims) {
        return claims.stream()
            .collect(Collectors.groupingBy(this::extractDependencyKey));
    }
    
    /**
     * Extracts a key representing the dependency relationship from processed data.
     * Expected format: "sourceApp -> targetApp" or similar.
     */
    private String extractDependencyKey(Claim claim) {
        String processedData = claim.getProcessedData();
        if (processedData != null && processedData.contains("->")) {
            return processedData.trim().toLowerCase();
        }
        // Fallback: use claim ID if no standard format
        return claim.getId();
    }
    
    /**
     * Resolves a group of conflicting claims using weighted voting.
     */
    private Claim resolveConflictingClaims(List<Claim> conflictingClaims) {
        // Calculate weighted scores for each claim
        Map<Claim, Double> weightedScores = new HashMap<>();
        
        for (Claim claim : conflictingClaims) {
            double weight = calculateClaimWeight(claim, conflictingClaims);
            weightedScores.put(claim, weight);
            logger.debug("Claim {} weighted score: {:.3f}", claim.getId(), weight);
        }
        
        // Select claim with highest weighted score
        Claim winner = weightedScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(conflictingClaims.get(0));
        
        // Create resolved claim with updated confidence score
        double finalConfidence = weightedScores.get(winner);
        logger.info("Resolved conflict: selected claim {} with confidence {}", 
            winner.getId(), finalConfidence);
        
        // Ensure the resolved claim always has a confidence score
        ConfidenceScore resolvedScore = ConfidenceScore.of(Math.min(1.0, Math.max(0.1, finalConfidence)));
        
        return Claim.builder()
            .id(winner.getId() + "_resolved")
            .sourceType("CONFLICT_RESOLVED")
            .rawData(winner.getRawData())
            .processedData(winner.getProcessedData())
            .timestamp(Instant.now())
            .confidenceScore(resolvedScore)
            .build();
    }
    
    /**
     * Calculates the weighted score for a claim based on multiple factors.
     */
    private double calculateClaimWeight(Claim claim, List<Claim> allClaims) {
        double baseConfidence = claim.getConfidenceScore() != null ? 
            claim.getConfidenceScore().getValue() : 0.5;
        
        // Factor 1: Source priority weight
        double sourcePriority = SOURCE_PRIORITIES.getOrDefault(
            claim.getSourceType().toUpperCase(), 0.5);
        
        // Factor 2: Recency weight
        double recencyWeight = calculateRecencyWeight(claim);
        
        // Factor 3: Frequency weight (how often this dependency appears)
        double frequencyWeight = calculateFrequencyWeight(claim, allClaims);
        
        // Combined weighted score
        double weightedScore = baseConfidence * sourcePriority * recencyWeight * frequencyWeight;
        
        logger.debug("Claim {} weights - base: {:.3f}, source: {:.3f}, recency: {:.3f}, frequency: {:.3f}, final: {:.3f}",
            claim.getId(), baseConfidence, sourcePriority, recencyWeight, frequencyWeight, weightedScore);
        
        return weightedScore;
    }
    
    /**
     * Calculates recency weight - newer claims get higher weight.
     */
    private double calculateRecencyWeight(Claim claim) {
        if (claim.getTimestamp() == null) {
            return 0.8; // Default for missing timestamp
        }
        
        long hoursAge = Duration.between(claim.getTimestamp(), Instant.now()).toHours();
        
        if (hoursAge <= RECENCY_THRESHOLD_HOURS) {
            return 1.0; // Full weight for recent claims
        } else {
            // Exponential decay for older claims
            double decayFactor = Math.pow(RECENCY_DECAY_FACTOR, 
                (hoursAge - RECENCY_THRESHOLD_HOURS) / 24.0);
            return Math.max(0.1, decayFactor); // Minimum 10% weight
        }
    }
    
    /**
     * Calculates frequency weight - claims that appear more often get higher weight.
     */
    private double calculateFrequencyWeight(Claim claim, List<Claim> allClaims) {
        String dependencyKey = extractDependencyKey(claim);
        
        long frequency = allClaims.stream()
            .mapToLong(c -> extractDependencyKey(c).equals(dependencyKey) ? 1 : 0)
            .sum();
        
        // Normalize frequency (1-3 occurrences = normal, 4+ = high confidence boost)
        if (frequency <= 1) return 1.0;
        if (frequency <= 3) return 1.1;
        return 1.2; // Cap frequency boost at 20%
    }
    
    /**
     * Applies business rules for manual overrides.
     * TODO: Implement configurable business rules from database or config file.
     */
    public Claim applyBusinessRules(Claim claim) {
        // TODO: Add configurable business rule application
        // Examples:
        // - Force certain dependencies to always be true/false
        // - Apply organization-specific validation rules
        // - Handle known exceptions or edge cases
        
        logger.debug("Applied business rules to claim: {}", claim.getId());
        return claim;
    }
}

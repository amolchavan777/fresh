package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.model.core.Dependency;
import com.enterprise.dependency.model.core.DependencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * InferenceEngine applies machine learning algorithms and heuristics to
 * infer implicit dependencies from observational data.
 * 
 * <p>Inference strategies include:
 * <ul>
 *   <li>Pattern recognition for common dependency patterns</li>
 *   <li>Temporal correlation analysis</li>
 *   <li>Service naming convention inference</li>
 *   <li>Network traffic pattern analysis</li>
 *   <li>Transaction correlation across service boundaries</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   InferenceEngine engine = new InferenceEngine();
 *   List&lt;Claim&gt; observedClaims = loadObservationalData();
 *   List&lt;Dependency&gt; inferredDeps = engine.inferDependencies(observedClaims);
 * </pre>
 */
@Service
public class InferenceEngine {
    private static final Logger logger = LoggerFactory.getLogger(InferenceEngine.class);
    
    // Confidence thresholds for different inference types
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.4;
    
    // Database/storage patterns
    private static final Pattern DB_PATTERN = Pattern.compile(
        ".*(db|database|cache|redis|mongo|postgres|mysql|oracle).*", 
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Infers implicit dependencies from a collection of claims.
     * 
     * @param claims Observed claims from various data sources
     * @return List of inferred dependencies with confidence scores
     */
    public List<Dependency> inferDependencies(List<Claim> claims) {
        if (claims == null || claims.isEmpty()) {
            logger.warn("No claims provided for dependency inference");
            return Collections.emptyList();
        }
        
        logger.info("Starting dependency inference on {} claims", claims.size());
        
        List<Dependency> inferredDependencies = new ArrayList<>();
        
        // Group claims by source-target pairs for correlation analysis
        Map<String, List<Claim>> claimGroups = groupClaimsByDependency(claims);
        
        for (Map.Entry<String, List<Claim>> entry : claimGroups.entrySet()) {
            String dependencyKey = entry.getKey();
            List<Claim> relatedClaims = entry.getValue();
            
            // Apply various inference strategies
            Optional<Dependency> inferredDep = inferFromClaimGroup(dependencyKey, relatedClaims);
            inferredDep.ifPresent(inferredDependencies::add);
        }
        
        // Apply cross-dependency inference patterns
        inferredDependencies.addAll(inferCrossDependencyPatterns(claims));
        
        // Apply temporal correlation analysis
        inferredDependencies.addAll(inferTemporalCorrelations(claims));
        
        logger.info("Inferred {} dependencies from {} claim groups", 
            inferredDependencies.size(), claimGroups.size());
        
        return deduplicateDependencies(inferredDependencies);
    }

    /**
     * Groups claims by their dependency relationship key.
     */
    private Map<String, List<Claim>> groupClaimsByDependency(List<Claim> claims) {
        return claims.stream()
            .collect(Collectors.groupingBy(this::extractDependencyKey));
    }

    /**
     * Extracts a standardized dependency key from a claim.
     */
    private String extractDependencyKey(Claim claim) {
        String processedData = claim.getProcessedData();
        if (processedData != null && processedData.contains("->")) {
            return processedData.trim().toLowerCase();
        }
        return claim.getId();
    }

    /**
     * Infers a dependency from a group of related claims.
     */
    private Optional<Dependency> inferFromClaimGroup(String dependencyKey, List<Claim> claims) {
        if (claims.isEmpty()) {
            return Optional.empty();
        }
        
        // Extract source and target from dependency key
        String[] parts = dependencyKey.split("\\s*->\\s*");
        if (parts.length != 2) {
            logger.debug("Unable to parse dependency key: {}", dependencyKey);
            return Optional.empty();
        }
        
        String sourceApp = parts[0].trim();
        String targetApp = parts[1].trim();
        
        // Determine dependency type through pattern analysis
        DependencyType dependencyType = inferDependencyType(targetApp, claims);
        
        // Calculate confidence based on multiple factors
        double confidence = calculateInferenceConfidence(claims, dependencyType);
        
        if (confidence < LOW_CONFIDENCE_THRESHOLD) {
            logger.debug("Confidence too low for dependency: {} ({})", dependencyKey, confidence);
            return Optional.empty();
        }
        
        Dependency dependency = Dependency.builder()
            .sourceAppId(sourceApp)
            .targetAppId(targetApp)
            .type(dependencyType)
            .confidenceScore(ConfidenceScore.of(confidence))
            .build();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Inferred dependency: {} -> {} [{}]", sourceApp, targetApp, dependencyType);
        }
        
        return Optional.of(dependency);
    }

    /**
     * Infers the type of dependency based on naming patterns and claim context.
     */
    private DependencyType inferDependencyType(String targetApp, List<Claim> claims) {
        // Check for database patterns
        if (isDatabase(targetApp)) {
            return DependencyType.DATABASE;
        }
        
        // Check for API patterns in claims
        boolean hasApiPattern = claims.stream()
            .anyMatch(claim -> claim.getRawData() != null && 
                     (claim.getRawData().contains("/api/") || 
                      claim.getRawData().contains("GET ") || 
                      claim.getRawData().contains("POST ")));
        
        if (hasApiPattern) {
            return DependencyType.API;
        }
        
        // Check for build/compile time dependencies
        boolean isBuildDependency = claims.stream()
            .anyMatch(claim -> claim.getSourceType().equals("CODEBASE"));
        
        if (isBuildDependency) {
            return DependencyType.BUILD;
        }
        
        // Default to runtime dependency
        return DependencyType.RUNTIME;
    }

    /**
     * Determines if a target appears to be a database.
     */
    private boolean isDatabase(String target) {
        return DB_PATTERN.matcher(target).matches();
    }

    /**
     * Calculates inference confidence based on multiple factors.
     */
    private double calculateInferenceConfidence(List<Claim> claims, DependencyType dependencyType) {
        double baseConfidence = 0.5;
        
        // Factor 1: Number of supporting claims - more aggressive boost for multiple claims
        double claimFrequency = Math.min(1.0, claims.size() / 3.0); // Max at 3 claims (more aggressive)
        if (claims.size() > 1) {
            claimFrequency += 0.3; // Significant bonus for having multiple claims
            claimFrequency = Math.min(1.0, claimFrequency);
        }
        
        // Factor 2: Source diversity (different source types = higher confidence)
        long uniqueSources = claims.stream()
            .map(Claim::getSourceType)
            .distinct()
            .count();
        double sourceDiversity = Math.min(1.0, uniqueSources / 3.0); // Max at 3 sources
        
        // Factor 3: Individual claim confidence scores (weighted by source reliability)
        double avgClaimConfidence = claims.stream()
            .filter(claim -> claim.getConfidenceScore() != null)
            .mapToDouble(claim -> {
                double claimConfidence = claim.getConfidenceScore().getValue();
                // Apply source reliability weight
                double weight = getSourceReliabilityWeight(claim.getSourceType());
                return claimConfidence * weight;
            })
            .average()
            .orElse(0.5);
        
        // Factor 4: Source quality bonus (for high-reliability sources)
        double sourceQualityBonus = calculateSourceQualityBonus(claims);
        
        // Factor 5: Dependency type specificity bonus
        double typeBonus = getTypeSpecificityBonus(dependencyType);
        
        // Factor 6: Recency factor
        double recencyFactor = calculateRecencyFactor(claims);
        
        // Weighted combination - emphasizing multiple claims and source diversity more
        double confidence = (baseConfidence * 0.05) +
                          (claimFrequency * 0.3) +      // Weight for multiple claims
                          (sourceDiversity * 0.2) +     // Weight for source diversity
                          (avgClaimConfidence * 0.25) +  // Weight for individual confidence (with source weighting)
                          (sourceQualityBonus * 0.1) +   // Weight for source quality
                          (typeBonus * 0.05) +          // Weight for type bonus
                          (recencyFactor * 0.05);
        
        return Math.min(1.0, Math.max(0.0, confidence));
    }

    /**
     * Returns a bonus score based on how specific the dependency type inference is.
     */
    private double getTypeSpecificityBonus(DependencyType type) {
        switch (type) {
            case DATABASE:
            case API:
                return 0.2; // High specificity
            case BUILD:
            case RUNTIME:
                return 0.1; // Medium specificity
            case NETWORK:
            case OTHER:
            default:
                return 0.0; // No bonus for unknown types
        }
    }

    /**
     * Calculates recency factor - more recent claims get higher weight.
     */
    private double calculateRecencyFactor(List<Claim> claims) {
        Instant now = Instant.now();
        
        return claims.stream()
            .filter(claim -> claim.getTimestamp() != null)
            .mapToDouble(claim -> {
                long hoursAgo = java.time.Duration.between(claim.getTimestamp(), now).toHours();
                if (hoursAgo <= 1) return 1.0;
                if (hoursAgo <= 24) return 0.8;
                if (hoursAgo <= 168) return 0.6; // 1 week
                return 0.3;
            })
            .average()
            .orElse(0.5);
    }

    /**
     * Infers dependencies based on cross-dependency patterns.
     * TODO: Implement pattern recognition algorithms for transitive dependencies.
     */
    @SuppressWarnings("unused")
    private List<Dependency> inferCrossDependencyPatterns(List<Claim> allClaims) {
        // Placeholder for future implementation
        return new ArrayList<>();
    }

    /**
     * Infers dependencies based on temporal correlation analysis.
     * TODO: Implement temporal correlation analysis for cascading patterns.
     */
    @SuppressWarnings("unused")
    private List<Dependency> inferTemporalCorrelations(List<Claim> allClaims) {
        // Placeholder for future implementation  
        return new ArrayList<>();
    }

    /**
     * Removes duplicate dependencies from the inferred list.
     */
    private List<Dependency> deduplicateDependencies(List<Dependency> dependencies) {
        Map<String, Dependency> uniqueDeps = new HashMap<>();
        
        for (Dependency dep : dependencies) {
            String key = dep.getSourceAppId() + "->" + 
                        dep.getTargetAppId() + ":" + 
                        dep.getType();
            
            // Use computeIfAbsent or update if higher confidence
            uniqueDeps.compute(key, (k, existing) -> {
                if (existing == null || 
                    dep.getConfidenceScore().getValue() > existing.getConfidenceScore().getValue()) {
                    return dep;
                }
                return existing;
            });
        }
        
        List<Dependency> deduplicated = new ArrayList<>(uniqueDeps.values());
        
        if (deduplicated.size() < dependencies.size()) {
            logger.info("Deduplicated {} dependencies to {} unique dependencies", 
                dependencies.size(), deduplicated.size());
        }
        
        return deduplicated;
    }

    /**
     * Returns reliability weight for different source types.
     */
    private double getSourceReliabilityWeight(String sourceType) {
        if (sourceType == null) return 1.0;
        
        switch (sourceType.toUpperCase()) {
            case "CODEBASE":
                return 1.3; // High reliability
            case "API_GATEWAY":
            case "ROUTER_LOG":
                return 1.1; // Medium reliability
            case "NETWORK":
                return 0.9; // Lower reliability
            default:
                return 1.0; // Default weight
        }
    }

    /**
     * Calculates bonus for having high-quality sources.
     */
    private double calculateSourceQualityBonus(List<Claim> claims) {
        long highQualitySources = claims.stream()
            .filter(claim -> "CODEBASE".equalsIgnoreCase(claim.getSourceType()) || 
                           "API_GATEWAY".equalsIgnoreCase(claim.getSourceType()))
            .count();
        
        return Math.min(0.3, highQualitySources * 0.15); // Up to 0.3 bonus
    }
}

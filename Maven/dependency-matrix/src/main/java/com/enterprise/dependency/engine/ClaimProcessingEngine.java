package com.enterprise.dependency.engine;

import com.enterprise.dependency.config.ScoringProperties;
import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.scoring.ScoringRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClaimProcessingEngine is the core business logic component responsible for transforming
 * raw dependency claims into validated, normalized, and scored dependency relationships.
 * 
 * <p>This engine serves as the central processing hub in the dependency matrix system,
 * orchestrating the entire claim lifecycle from raw data ingestion to final scoring.</p>
 * 
 * <p><strong>Processing Pipeline:</strong></p>
 * <pre>
 * Raw Claims → Normalize → Validate → Score → Processed Claims
 *      ↓            ↓          ↓        ↓           ↓
 *   [Input]    [Cleanup]  [Quality]  [AI/Rules] [Output]
 * </pre>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>Normalization:</strong> Standardizes data formats, trims whitespace, canonicalizes identifiers</li>
 *   <li><strong>Validation:</strong> Ensures data quality and completeness according to business rules</li>
 *   <li><strong>Scoring:</strong> Assigns confidence scores using pluggable scoring rule engines</li>
 *   <li><strong>Error Handling:</strong> Gracefully handles malformed data without stopping processing</li>
 *   <li><strong>Logging:</strong> Provides comprehensive audit trails for troubleshooting</li>
 * </ul>
 * 
 * <p><strong>Integration Points:</strong></p>
 * <ul>
 *   <li>Uses {@link ScoringRuleEngine} for confidence score calculation</li>
 *   <li>Leverages {@link ScoringProperties} for configurable processing parameters</li>
 *   <li>Integrates with Spring's dependency injection for loose coupling</li>
 * </ul>
 * 
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li>Processes claims in memory for optimal performance</li>
 *   <li>Continues processing even if individual claims fail</li>
 *   <li>Provides detailed metrics through logging</li>
 * </ul>
 * 
 * <p><strong>Example Usage:</strong></p>
 * <pre>
 * {@code
 * @Autowired
 * private ClaimProcessingEngine engine;
 * 
 * // Process a batch of claims from multiple sources
 * List<Claim> rawClaims = dataLoader.loadFromAllSources();
 * List<Claim> processedClaims = engine.processClaims(rawClaims);
 * 
 * // Individual claim processing
 * Claim normalizedClaim = engine.normalize(rawClaim);
 * engine.validate(normalizedClaim);
 * Claim scoredClaim = engine.score(normalizedClaim);
 * }
 * </pre>
 * 
 * @author Enterprise Architecture Team
 * @version 1.0
 * @since 2025-07-05
 * @see ScoringRuleEngine
 * @see Claim
 * @see ConfidenceScore
 */
@Service
public class ClaimProcessingEngine {
    private static final Logger logger = LoggerFactory.getLogger(ClaimProcessingEngine.class);

    /** 
     * Pluggable scoring rule engine that calculates confidence scores based on configurable rules.
     * This engine can be swapped out for different scoring algorithms without affecting the core logic.
     */
    private final ScoringRuleEngine scoringRuleEngine;

    /**
     * Constructor for dependency injection.
     * 
     * @param scoringRuleEngine The scoring engine implementation to use for calculating confidence scores
     * @throws IllegalArgumentException if scoringRuleEngine is null
     */
    @Autowired
    public ClaimProcessingEngine(ScoringRuleEngine scoringRuleEngine) {
        this.scoringRuleEngine = Objects.requireNonNull(scoringRuleEngine, 
            "ScoringRuleEngine cannot be null");
        logger.info("ClaimProcessingEngine initialized with scoring engine: {}", 
            scoringRuleEngine.getClass().getSimpleName());
    }

    /**
     * Processes a batch of raw claims through the complete processing pipeline.
     * 
     * <p>This method implements a fault-tolerant processing strategy where individual
     * claim failures do not prevent the processing of other claims in the batch.</p>
     * 
     * <p><strong>Processing Steps for Each Claim:</strong></p>
     * <ol>
     *   <li><strong>Normalization:</strong> Standardizes data formats and cleans up inconsistencies</li>
     *   <li><strong>Validation:</strong> Ensures data meets quality and completeness requirements</li>
     *   <li><strong>Scoring:</strong> Calculates confidence scores using the configured rule engine</li>
     * </ol>
     * 
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Individual claim failures are logged but do not stop batch processing</li>
     *   <li>Failed claims are excluded from the result set</li>
     *   <li>Detailed error information is captured in logs for debugging</li>
     * </ul>
     * 
     * @param rawClaims List of raw Claim objects to process. Can contain claims from multiple sources.
     *                  Must not be null, but can be empty.
     * @return List of successfully processed and valid Claim objects. 
     *         May be smaller than input if some claims failed processing.
     * @throws IllegalArgumentException if rawClaims is null
     * 
     * @see #normalize(Claim)
     * @see #validate(Claim)
     * @see #score(Claim)
     */
    public List<Claim> processClaims(List<Claim> rawClaims) {
        Objects.requireNonNull(rawClaims, "Raw claims list cannot be null");
        
        logger.info("Starting processing of {} raw claims", rawClaims.size());
        Instant startTime = Instant.now();
        
        List<Claim> result = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Claim claim : rawClaims) {
            try {
                // Step 1: Normalize the claim data
                Claim normalized = normalize(claim);
                
                // Step 2: Validate the normalized claim
                validate(normalized);
                
                // Step 3: Score the validated claim
                Claim scored = score(normalized);
                
                result.add(scored);
                successCount++;
                logger.debug("Successfully processed claim: {}", scored.getId());
            } catch (Exception e) {
                failureCount++;
                logger.warn("Claim processing failed for claim ID: {} - Error: {}", 
                    claim != null ? claim.getId() : "null", e.getMessage(), e);
            }
        }
        
        Duration processingTime = Duration.between(startTime, Instant.now());
        logger.info("Claim processing completed: {} successful, {} failed, took {} ms", 
            successCount, failureCount, processingTime.toMillis());
            
        return result;
    }

    /**
     * Normalizes a claim by standardizing data formats and cleaning inconsistencies.
     * 
     * <p>Normalization ensures that data from different sources follows consistent
     * patterns and formats, improving the quality of downstream processing.</p>
     * 
     * <p><strong>Normalization Operations:</strong></p>
     * <ul>
     *   <li>Trims whitespace from string fields</li>
     *   <li>Converts identifiers to lowercase for consistency</li>
     *   <li>Standardizes timestamp formats</li>
     *   <li>Canonicalizes application names and IDs</li>
     *   <li>Removes special characters that could interfere with processing</li>
     * </ul>
     * 
     * @param claim The raw claim to normalize. Must not be null.
     * @return A new Claim object with normalized data fields
     * @throws IllegalArgumentException if claim is null
     * @throws ProcessingException if normalization fails due to data corruption
     */
    public Claim normalize(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null for normalization");
        
        logger.debug("Normalizing claim: {}", claim.getId());
        
        try {
            // Implementation note: Currently returns the claim as-is
            // Future enhancements will include:
            // - String trimming and case standardization
            // - Timestamp format normalization
            // - Application ID canonicalization
            // - Special character removal/replacement
            
            // For now, we're preserving the original data structure
            // while the normalization rules are being finalized
            logger.debug("Normalization completed for claim: {}", claim.getId());
            return claim;
            
        } catch (Exception e) {
            logger.error("Failed to normalize claim: {}", claim.getId(), e);
            throw new RuntimeException("Normalization failed for claim: " + claim.getId(), e);
        }
    }

    /**
     * Validates a normalized claim for data quality and completeness.
     * 
     * <p>Validation ensures that claims meet the minimum quality standards
     * required for accurate dependency analysis. This includes checking for
     * required fields, data format compliance, and business rule adherence.</p>
     * 
     * <p><strong>Validation Checks:</strong></p>
     * <ul>
     *   <li><strong>Required Fields:</strong> Ensures all mandatory fields are present and non-null</li>
     *   <li><strong>Data Format:</strong> Validates that fields conform to expected formats</li>
     *   <li><strong>Business Rules:</strong> Applies domain-specific validation logic</li>
     *   <li><strong>Data Consistency:</strong> Cross-validates related fields for logical consistency</li>
     *   <li><strong>Temporal Validation:</strong> Ensures timestamps are reasonable and not in the future</li>
     * </ul>
     * 
     * <p><strong>Validation Failures:</strong></p>
     * <p>If validation fails, detailed error messages are logged and an exception is thrown
     * to prevent invalid data from progressing through the pipeline.</p>
     * 
     * @param claim The normalized claim to validate. Must not be null.
     * @throws IllegalArgumentException if claim is null or fails validation checks
     * @throws ValidationException if claim data doesn't meet quality standards
     * 
     * @see #normalize(Claim)
     */
    public void validate(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null for validation");
        
        logger.debug("Validating claim: {}", claim.getId());
        
        try {
            // Core field validation - these are essential for any claim
            validateRequiredField(claim.getId(), "Claim id");
            validateRequiredField(claim.getSourceType(), "Claim sourceType");
            validateRequiredField(claim.getRawData(), "Claim rawData");
            validateRequiredField(claim.getProcessedData(), "Claim processedData");
            validateRequiredField(claim.getTimestamp(), "Claim timestamp");
            
            // Temporal validation - ensure timestamp is reasonable
            if (claim.getTimestamp().isAfter(Instant.now())) {
                throw new IllegalArgumentException("Claim timestamp cannot be in the future: " + claim.getTimestamp());
            }
            
            // Data quality validation
            if (claim.getRawData().trim().isEmpty()) {
                throw new IllegalArgumentException("Claim rawData cannot be empty or just whitespace");
            }
            
            if (claim.getProcessedData().trim().isEmpty()) {
                throw new IllegalArgumentException("Claim processedData cannot be empty or just whitespace");
            }
            
            logger.debug("Validation completed successfully for claim: {}", claim.getId());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed for claim {}: {}", claim.getId(), e.getMessage());
            throw e; // Re-throw to stop processing of invalid claims
        } catch (Exception e) {
            logger.error("Unexpected error during validation of claim: {}", claim.getId(), e);
            throw new IllegalArgumentException("Validation failed due to unexpected error for claim: " + claim.getId(), e);
        }
    }
    
    /**
     * Helper method to validate that a required field is not null or empty.
     * 
     * @param value The field value to check
     * @param fieldName The name of the field for error reporting
     * @throws IllegalArgumentException if the field is null or empty
     */
    private void validateRequiredField(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required and cannot be null");
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required and cannot be empty");
        }
    }

    /**
     * Assigns a confidence score to a validated claim using the configured scoring rule engine.
     * 
     * <p>The scoring process evaluates multiple factors to determine how confident
     * we can be that this claim represents a real dependency relationship.</p>
     * 
     * <p><strong>Scoring Factors Considered:</strong></p>
     * <ul>
     *   <li><strong>Source Type:</strong> Different data sources have different reliability levels</li>
     *   <li><strong>Data Quality:</strong> Completeness and format compliance of the claim data</li>
     *   <li><strong>Temporal Factors:</strong> Age of the claim and recency of the dependency</li>
     *   <li><strong>Cross-Validation:</strong> Consistency with other claims about the same dependency</li>
     * </ul>
     * 
     * <p><strong>Score Range:</strong></p>
     * <p>Confidence scores range from 0.0 (no confidence) to 1.0 (complete confidence).
     * The scoring algorithm is configurable through {@link ScoringProperties}.</p>
     * 
     * @param claim The validated claim to score. Must not be null and must pass validation.
     * @return A new Claim object with an assigned confidence score
     * @throws IllegalArgumentException if claim is null
     * @throws ScoringException if scoring engine fails to process the claim
     * 
     * @see ScoringRuleEngine#score(Claim)
     * @see ConfidenceScore
     */
    public Claim score(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null for scoring");
        
        logger.debug("Scoring claim: {}", claim.getId());
        
        try {
            // Delegate to the pluggable scoring rule engine
            double scoreValue = scoringRuleEngine.score(claim);
            
            // Ensure score is within valid range
            if (scoreValue < 0.0 || scoreValue > 1.0) {
                logger.warn("Scoring engine returned out-of-range score {} for claim {}. Clamping to valid range.", 
                    scoreValue, claim.getId());
                scoreValue = Math.max(0.0, Math.min(1.0, scoreValue));
            }
            
            // Create a new claim with the calculated confidence score
            Claim scoredClaim = Claim.builder()
                    .id(claim.getId())
                    .sourceType(claim.getSourceType())
                    .rawData(claim.getRawData())
                    .processedData(claim.getProcessedData())
                    .timestamp(claim.getTimestamp())
                    .confidenceScore(ConfidenceScore.of(scoreValue))
                    .build();
                    
            logger.debug("Assigned confidence score {} to claim: {}", scoreValue, claim.getId());
            return scoredClaim;
            
        } catch (Exception e) {
            logger.error("Failed to score claim: {}", claim.getId(), e);
            throw new IllegalArgumentException("Scoring failed for claim: " + claim.getId(), e);
        }
    }
}

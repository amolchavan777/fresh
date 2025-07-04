package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Claim;

/**
 * Interface for pluggable scoring rule engines.
 * Implementations can use config, scripts, or external services.
 */
public interface ScoringRuleEngine {
    /**
     * Calculates a confidence score for the given claim.
     * @param claim Claim to score
     * @return confidence score (0.0 to 1.0)
     */
    double score(Claim claim);
}

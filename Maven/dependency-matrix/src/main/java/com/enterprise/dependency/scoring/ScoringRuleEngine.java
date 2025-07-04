package com.enterprise.dependency.scoring;

import com.enterprise.dependency.model.core.Claim;

/**
 * Interface for pluggable scoring rule engines.
 */
public interface ScoringRuleEngine {
    /**
     * Calculates a confidence score for the given claim.
     * @param claim the claim to score
     * @return the confidence score (0.0 - 1.0)
     */
    double score(Claim claim);
}

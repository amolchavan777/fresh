package com.enterprise.dependency.scoring;

import com.enterprise.dependency.model.core.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of ScoringRuleEngine using config-based rules.
 */
@Component
public class DefaultScoringRuleEngine implements ScoringRuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(DefaultScoringRuleEngine.class);

    @Override
    public double score(Claim claim) {
        double base = 0.5;
        if (claim.getSourceType() != null) {
            switch (claim.getSourceType().toUpperCase()) {
                case "CODEBASE": base = 0.95; break;
                case "ROUTER_LOG": base = 0.85; break;
                case "API_GATEWAY": base = 0.80; break;
                default: base = 0.5;
            }
        }
        // Example: penalty for missing processedData
        if (claim.getProcessedData() == null || claim.getProcessedData().length() < 5) {
            base -= 0.15;
            logger.warn("Processed data penalty applied for claim {}", claim.getId());
        }
        // Clamp to [0,1]
        return Math.max(0.0, Math.min(1.0, base));
    }
}

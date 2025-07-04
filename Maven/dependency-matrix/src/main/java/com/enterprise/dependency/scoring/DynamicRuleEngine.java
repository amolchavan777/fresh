package com.enterprise.dependency.scoring;

import com.enterprise.dependency.model.core.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DynamicRuleEngine: Example of a pluggable, dynamic rule-based scoring engine.
 * In a real system, rules could be loaded from DB, config, or a UI.
 */
@Primary // Makes this the default engine if multiple are present
@Component
public class DynamicRuleEngine implements ScoringRuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(DynamicRuleEngine.class);

    private RuleConfig ruleConfig = new RuleConfig();
    private static final String RULES_FILE = "dynamic-rules.ser";

    @PostConstruct
    public void loadRules() {
        // Try to load from file, else use defaults
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RULES_FILE))) {
            ruleConfig = (RuleConfig) ois.readObject();
            logger.info("Loaded rules from file: {}", RULES_FILE);
        } catch (Exception e) {
            logger.warn("Could not load rules from file, using defaults.");
            // Defaults
            ruleConfig.put("criticalAppBoost", 0.2);
            ruleConfig.put("testDataPenalty", -0.15);
            ruleConfig.put("codebaseBoost", 0.3);
            ruleConfig.put("routerLogBoost", 0.15);
            ruleConfig.put("apiGatewayBoost", 0.10);
            ruleConfig.put("processedDataPenalty", -0.15);
            ruleConfig.put("idPenalty", -0.1);
            ruleConfig.put("oldClaimPenalty", -0.1);
            ruleConfig.put("oldClaimDays", 30);
        }
    }

    public void saveRules() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RULES_FILE))) {
            oos.writeObject(ruleConfig);
            logger.info("Saved rules to file: {}", RULES_FILE);
        } catch (IOException e) {
            logger.error("Failed to save rules to file: {}", RULES_FILE, e);
        }
    }

    public RuleConfig getRuleConfig() { return ruleConfig; }
    public void updateRules(Map<String, Object> updates) {
        ruleConfig.putAll(updates);
        saveRules();
    }

    @Override
    public double score(Claim claim) {
        double score = 0.5;
        // Rule 1: Boost for critical app in processedData
        if (claim.getProcessedData() != null && java.util.regex.Pattern.compile("critical-app", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(claim.getProcessedData()).find()) {
            score += toDouble(ruleConfig.get("criticalAppBoost"), 0.2);
        }
        // Rule 2: Penalty for claims with 'test' in rawData
        if (claim.getRawData() != null && claim.getRawData().toLowerCase().contains("test")) {
            score += toDouble(ruleConfig.get("testDataPenalty"), -0.15);
        }
        // Rule 3: Source-based logic
        if (claim.getSourceType() != null) {
            switch (claim.getSourceType().toUpperCase()) {
                case "CODEBASE": score += toDouble(ruleConfig.get("codebaseBoost"), 0.3); break;
                case "ROUTER_LOG": score += toDouble(ruleConfig.get("routerLogBoost"), 0.15); break;
                case "API_GATEWAY": score += toDouble(ruleConfig.get("apiGatewayBoost"), 0.10); break;
            }
        }
        // Rule 4: Penalty for missing processedData
        if (claim.getProcessedData() == null || claim.getProcessedData().length() < 5) {
            score += toDouble(ruleConfig.get("processedDataPenalty"), -0.15);
        }
        // Rule 5: Penalty for missing id
        if (claim.getId() == null || claim.getId().isEmpty()) {
            score += toDouble(ruleConfig.get("idPenalty"), -0.1);
        }
        // Rule 6: Penalty for old claims (older than N days)
        if (claim.getTimestamp() != null) {
            long ageDays = java.time.Duration.between(claim.getTimestamp(), java.time.Instant.now()).toDays();
            int oldDays = toInt(ruleConfig.get("oldClaimDays"), 30);
            if (ageDays > oldDays) {
                score += toDouble(ruleConfig.get("oldClaimPenalty"), -0.1);
            }
        }
        // Clamp to [0,1]
        score = Math.max(0.0, Math.min(1.0, score));
        return score;
    }

    private double toDouble(Object o, double def) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return def; }
    }
    private int toInt(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return def; }
    }
}

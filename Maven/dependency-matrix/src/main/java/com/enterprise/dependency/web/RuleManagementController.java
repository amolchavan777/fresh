package com.enterprise.dependency.web;

import com.enterprise.dependency.scoring.DynamicRuleEngine;
import com.enterprise.dependency.scoring.RuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for managing dynamic scoring rules.
 * This is a simple in-memory example for demonstration.
 */
@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"}) // Allow Vite dev server on multiple ports
public class RuleManagementController {
    private final DynamicRuleEngine dynamicRuleEngine;
    // In-memory map to simulate rule storage (key: rule name, value: rule value)
    private final Map<String, Object> rules = new ConcurrentHashMap<>();

    @Autowired
    public RuleManagementController(DynamicRuleEngine dynamicRuleEngine) {
        this.dynamicRuleEngine = dynamicRuleEngine;
        // Example: initialize with current rule values
        rules.put("criticalAppBoost", 0.2);
        rules.put("testDataPenalty", -0.15);
        rules.put("codebaseBoost", 0.3);
        rules.put("routerLogBoost", 0.15);
        rules.put("apiGatewayBoost", 0.10);
        rules.put("processedDataPenalty", -0.15);
        rules.put("idPenalty", -0.1);
        rules.put("oldClaimPenalty", -0.1);
        rules.put("oldClaimDays", 30);
    }

    @GetMapping
    public RuleConfig getRules() {
        return dynamicRuleEngine.getRuleConfig();
    }

    @PostMapping
    public RuleConfig updateRules(@RequestBody Map<String, Object> updates) {
        // Validate: only allow known keys and numeric values
        updates.entrySet().removeIf(e -> !dynamicRuleEngine.getRuleConfig().getRules().containsKey(e.getKey()));
        updates.entrySet().removeIf(e -> !(e.getValue() instanceof Number));
        dynamicRuleEngine.updateRules(updates);
        return dynamicRuleEngine.getRuleConfig();
    }
}

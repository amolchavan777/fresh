package com.enterprise.dependency.scoring;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RuleConfig holds all dynamic rule values for scoring.
 * Implements Serializable for future persistence.
 */
public class RuleConfig implements Serializable {
    private Map<String, Object> rules = new ConcurrentHashMap<>();

    public RuleConfig() {}
    public RuleConfig(Map<String, Object> initial) {
        if (initial != null) rules.putAll(initial);
    }
    public Map<String, Object> getRules() { return rules; }
    public void setRules(Map<String, Object> rules) { this.rules = rules; }
    public Object get(String key) { return rules.get(key); }
    public void put(String key, Object value) { rules.put(key, value); }
    public void putAll(Map<String, Object> updates) { rules.putAll(updates); }
}

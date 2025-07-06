package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TelemetryAdapter processes telemetry data from monitoring and observability systems
 * to extract dependency relationships and performance metrics.
 * 
 * <p>Supported telemetry formats:
 * <ul>
 *   <li>Prometheus metrics</li>
 *   <li>OpenTelemetry traces</li>
 *   <li>Application Performance Monitoring (APM) data</li>
 *   <li>Custom telemetry formats</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   TelemetryAdapter adapter = new TelemetryAdapter();
 *   List&lt;String&gt; telemetryData = readTelemetryMetrics();
 *   List&lt;Claim&gt; claims = adapter.parseTelemetryData(telemetryData);
 * </pre>
 * 
 * @author Enterprise Architecture Team
 * @version 1.0
 * @since 2025-07-05
 */
@Component
public class TelemetryAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryAdapter.class);
    
    // Prometheus HTTP request metrics pattern
    private static final Pattern PROMETHEUS_HTTP_PATTERN = Pattern.compile(
        "http_requests_total\\{.*?job=\"([^\"]+)\".*?instance=\"([^\"]+)\".*?target=\"([^\"]+)\".*?\\}\\s+(\\d+(?:\\.\\d+)?)"
    );
    
    // OpenTelemetry span pattern
    private static final Pattern OTEL_SPAN_PATTERN = Pattern.compile(
        "span\\{.*?service\\.name=\"([^\"]+)\".*?operation\\.name=\"([^\"]+)\".*?peer\\.service=\"([^\"]+)\".*?duration=(\\d+)ms.*?\\}"
    );
    
    // APM dependency pattern
    private static final Pattern APM_DEPENDENCY_PATTERN = Pattern.compile(
        "dependency\\{source=\"([^\"]+)\",target=\"([^\"]+)\",type=\"([^\"]+)\",response_time=(\\d+)ms,success_rate=([\\d.]+)\\}"
    );
    
    // Custom telemetry pattern for service calls
    private static final Pattern CUSTOM_TELEMETRY_PATTERN = Pattern.compile(
        "TELEMETRY\\s+(?<timestamp>[\\d\\-T:\\sZ]+)\\s+(?<source>[\\w\\-]+)\\s*->\\s*(?<target>[\\w\\-]+)\\s+(?<metric>\\w+)=(?<value>[\\d.]+)(?<unit>\\w*)"
    );

    /**
     * Parses telemetry data from various monitoring systems
     * 
     * @param telemetryData List of telemetry entries as strings
     * @return List of claims extracted from telemetry data
     */
    public List<Claim> parseTelemetryData(List<String> telemetryData) {
        List<Claim> claims = new ArrayList<>();
        
        if (telemetryData == null || telemetryData.isEmpty()) {
            logger.warn("No telemetry data provided for parsing");
            return claims;
        }
        
        logger.info("Parsing {} telemetry entries", telemetryData.size());
        
        for (int i = 0; i < telemetryData.size(); i++) {
            String entry = telemetryData.get(i);
            try {
                Claim claim = parseTelemetryEntry(entry);
                if (claim != null) {
                    claims.add(claim);
                    logger.debug("Parsed telemetry claim from line {}: {}", i + 1, claim);
                } else {
                    logger.debug("Could not parse telemetry entry: {}", entry);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse telemetry entry {}: {}", i + 1, entry, e);
            }
        }
        
        logger.info("Successfully parsed {} claims from telemetry data", claims.size());
        return claims;
    }
    
    /**
     * Parse a single telemetry entry
     */
    private Claim parseTelemetryEntry(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return null;
        }
        
        // Try different telemetry formats
        Claim claim = null;
        
        // Try Prometheus format
        claim = parsePrometheusMetric(entry);
        if (claim != null) return claim;
        
        // Try OpenTelemetry format
        claim = parseOpenTelemetrySpan(entry);
        if (claim != null) return claim;
        
        // Try APM format
        claim = parseApmDependency(entry);
        if (claim != null) return claim;
        
        // Try custom telemetry format
        claim = parseCustomTelemetry(entry);
        if (claim != null) return claim;
        
        logger.debug("No telemetry pattern matched for entry: {}", entry);
        return null;
    }
    
    /**
     * Parse Prometheus HTTP metrics
     */
    private Claim parsePrometheusMetric(String entry) {
        Matcher matcher = PROMETHEUS_HTTP_PATTERN.matcher(entry);
        if (matcher.find()) {
            String sourceService = matcher.group(1);
            String targetService = matcher.group(3);
            
            return createTelemetryClaim(
                sourceService, 
                targetService, 
                "prometheus-http",
                entry,
                0.8 // Good confidence for Prometheus metrics
            );
        }
        return null;
    }
    
    /**
     * Parse OpenTelemetry span data
     */
    private Claim parseOpenTelemetrySpan(String entry) {
        Matcher matcher = OTEL_SPAN_PATTERN.matcher(entry);
        if (matcher.find()) {
            String serviceName = matcher.group(1);
            String peerService = matcher.group(3);
            
            return createTelemetryClaim(
                serviceName,
                peerService,
                "opentelemetry-span", 
                entry,
                0.9 // High confidence for OpenTelemetry traces
            );
        }
        return null;
    }
    
    /**
     * Parse APM dependency data
     */
    private Claim parseApmDependency(String entry) {
        Matcher matcher = APM_DEPENDENCY_PATTERN.matcher(entry);
        if (matcher.find()) {
            String sourceService = matcher.group(1);
            String targetService = matcher.group(2);
            
            return createTelemetryClaim(
                sourceService,
                targetService,
                "apm-dependency",
                entry,
                0.85 // High confidence for APM data
            );
        }
        return null;
    }
    
    /**
     * Parse custom telemetry format
     */
    private Claim parseCustomTelemetry(String entry) {
        Matcher matcher = CUSTOM_TELEMETRY_PATTERN.matcher(entry);
        if (matcher.find()) {
            String source = matcher.group("source");
            String target = matcher.group("target");
            
            return createTelemetryClaim(
                source,
                target,
                "custom-telemetry",
                entry,
                0.7 // Moderate confidence for custom telemetry
            );
        }
        return null;
    }
    
    /**
     * Parse telemetry data from a list of log entries.
     * 
     * @param telemetryLogs List of telemetry log entries
     * @return List of claims representing telemetry-based dependencies
     */
    public List<Claim> parseLogData(List<String> telemetryLogs) {
        if (telemetryLogs == null || telemetryLogs.isEmpty()) {
            logger.warn("Empty telemetry data provided");
            return new ArrayList<>();
        }
        
        logger.info("Parsing {} telemetry log entries", telemetryLogs.size());
        List<Claim> claims = new ArrayList<>();
        
        for (String line : telemetryLogs) {
            try {
                // Try different formats to parse the line
                Claim claim = parseLineAuto(line.trim());
                if (claim != null) {
                    claims.add(claim);
                    logger.debug("Parsed telemetry claim: {}", claim.getProcessedData());
                }
            } catch (Exception e) {
                logger.debug("Failed to parse telemetry log line: {}", line, e);
            }
        }
        
        logger.info("Extracted {} telemetry dependency claims", claims.size());
        return claims;
    }
    
    /**
     * Auto-detect format and parse a telemetry line.
     */
    private Claim parseLineAuto(String line) {
        if (line.isEmpty()) return null;
        
        // Try Prometheus format
        Claim claim = parsePrometheusMetric(line);
        if (claim != null) return claim;
        
        // Try OpenTelemetry format
        claim = parseOpenTelemetrySpan(line);
        if (claim != null) return claim;
        
        // Try APM format
        claim = parseApmDependency(line);
        if (claim != null) return claim;
        
        // Try custom telemetry format
        claim = parseCustomTelemetry(line);
        if (claim != null) return claim;
        
        logger.debug("Could not parse telemetry line with any known format: {}", line);
        return null;
    }
    
    /**
     * Create a telemetry claim
     */
    private Claim createTelemetryClaim(String sourceApp, String targetApp, String type, String rawData, double confidence) {
        return Claim.builder()
            .id("telemetry_" + UUID.randomUUID().toString())
            .sourceType("TELEMETRY")
            .rawData(rawData)
            .processedData(String.format("%s -> %s (%s)", sourceApp, targetApp, type))
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(confidence))
            .build();
    }
}

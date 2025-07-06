package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.sources.RouterLogEntry;
import com.enterprise.dependency.model.core.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for parsing router log files and extracting dependency claims.
 * <p>
 * Usage example:
 * <pre>
 *   RouterLogAdapter adapter = new RouterLogAdapter();
 *   List<Claim> claims = adapter.parseLogFile(Path.of("router.log"));
 * </pre>
 */
@Component
public class RouterLogAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RouterLogAdapter.class);
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "(?<timestamp>\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?) \\[INFO] (?<sourceIp>[\\w.-]+) -> (?<targetIp>[\\w.-]+):(?<targetPort>\\d+) (?<protocol>\\w+)( (?<method>\\w+))?( (?<endpoint>[^ ]+))?( (?<statusCode>\\d+))?( (?<responseTime>\\d+)ms)?"
    );
    private static final DateTimeFormatter DATE_FORMATTER_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DATE_FORMATTER_ISO_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Parses a router log file and extracts dependency claims.
     * @param logFilePath Path to the log file
     * @return List of Claim objects
     */
    public List<Claim> parseLogFile(Path logFilePath) {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    RouterLogEntry entry = parseLogLine(line);
                    if (entry != null) {
                        Claim claim = toClaim(entry, line);
                        claims.add(claim);
                        logger.debug("Parsed claim from line {}: {}", lineNumber, claim);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineNumber, line, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading log file: {}", logFilePath, e);
        }
        return claims;
    }

    /**
     * Parses a single router log line into a RouterLogEntry.
     * @param line Log line
     * @return RouterLogEntry or null if not matched
     */
    public RouterLogEntry parseLogLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            logger.debug("Log line did not match pattern: {}", line);
            return null;
        }
        try {
            String timestampStr = matcher.group("timestamp");
            LocalDateTime timestamp = parseTimestamp(timestampStr);
            String sourceIp = matcher.group("sourceIp");
            String targetIp = matcher.group("targetIp");
            int targetPort = Integer.parseInt(matcher.group("targetPort"));
            String protocol = matcher.group("protocol");
            String method = matcher.group("method");
            String endpoint = matcher.group("endpoint");
            Integer statusCode = matcher.group("statusCode") != null ? Integer.parseInt(matcher.group("statusCode")) : null;
            Integer responseTime = matcher.group("responseTime") != null ? Integer.parseInt(matcher.group("responseTime")) : null;
            return RouterLogEntry.builder()
                    .timestamp(timestamp)
                    .sourceIp(sourceIp)
                    .targetIp(targetIp)
                    .targetPort(targetPort)
                    .protocol(protocol)
                    .method(method)
                    .endpoint(endpoint)
                    .statusCode(statusCode)
                    .responseTimeMs(responseTime)
                    .rawLine(line)
                    .build();
        } catch (Exception e) {
            logger.warn("Error parsing log line: {}", line, e);
            return null;
        }
    }

    /**
     * Parse timestamp from various formats
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        // Try different timestamp formats
        try {
            if (timestampStr.contains("T") && timestampStr.endsWith("Z")) {
                if (timestampStr.contains(".")) {
                    return LocalDateTime.parse(timestampStr, DATE_FORMATTER_ISO_MILLIS);
                } else {
                    return LocalDateTime.parse(timestampStr, DATE_FORMATTER_ISO);
                }
            } else {
                return LocalDateTime.parse(timestampStr, DATE_FORMATTER_SPACE);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse timestamp: {}", timestampStr, e);
            throw e;
        }
    }

    /**
     * Converts a RouterLogEntry to a standardized Claim object.
     * @param entry RouterLogEntry
     * @param rawLine Original log line
     * @return Claim
     */
    public Claim toClaim(RouterLogEntry entry, String rawLine) {
        // Create meaningful dependency claims from router logs
        String sourceApp = mapIpToApplication(entry.getSourceIp());
        String targetApp = mapIpToApplication(entry.getTargetIp());
        
        return Claim.builder()
                .id("router-claim-" + Math.abs(entry.hashCode()))
                .sourceType("ROUTER_LOG")
                .rawData(rawLine)
                .processedData(String.format("%s -> %s via %s%s", 
                    sourceApp, 
                    targetApp, 
                    entry.getProtocol(),
                    entry.getEndpoint() != null ? " " + entry.getEndpoint() : ""))
                .timestamp(java.time.Instant.now())
                .build();
    }
    
    /**
     * Map IP addresses to application names based on common patterns
     */
    private String mapIpToApplication(String ip) {
        // Simple IP-to-service mapping for demo purposes
        // In a real system, this would likely come from a configuration or service registry
        if ("user-service".equals(ip) || ip.contains("user")) return "user-service";
        if ("auth-service".equals(ip) || ip.contains("auth")) return "auth-service";
        if ("order-service".equals(ip) || ip.contains("order")) return "order-service";
        if ("payment-service".equals(ip) || ip.contains("payment")) return "payment-service";
        if ("notification-service".equals(ip) || ip.contains("notification")) return "notification-service";
        if ("api-gateway".equals(ip) || ip.contains("gateway")) return "api-gateway";
        
        // Fallback: try to extract service name from IP-like strings
        if (ip.contains("-")) {
            return ip;  // Assume it's already a service name
        }
        
        // Default fallback for actual IP addresses
        return "app-" + ip.replace(".", "-");
    }

    /**
     * Parses multiple router log entries from string data.
     * 
     * @param logData List of log entries as strings
     * @return List of claims extracted from the log data
     */
    public List<Claim> parseLogData(List<String> logData) {
        List<Claim> claims = new ArrayList<>();
        
        if (logData == null || logData.isEmpty()) {
            logger.warn("No log data provided for parsing");
            return claims;
        }
        
        logger.info("Parsing {} router log entries", logData.size());
        
        for (int i = 0; i < logData.size(); i++) {
            String line = logData.get(i);
            try {
                RouterLogEntry entry = parseLogLine(line);
                if (entry != null) {
                    Claim claim = toClaim(entry, line);
                    claims.add(claim);
                    logger.debug("Parsed claim from line {}: {}", i + 1, claim);
                } else {
                    logger.debug("Log line did not match pattern: {}", line);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse log line {}: {}", i + 1, line, e);
            }
        }
        
        logger.info("Successfully parsed {} claims from router log data", claims.size());
        return claims;
    }
}

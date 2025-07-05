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
            "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[INFO] (?<sourceIp>\\d+\\.\\d+\\.\\d+\\.\\d+) -> (?<targetIp>\\d+\\.\\d+\\.\\d+\\.\\d+):(?<targetPort>\\d+) (?<protocol>\\w+)( (?<method>\\w+))?( (?<endpoint>[^ ]+))?( (?<statusCode>\\d+))?( (?<responseTime>\\d+)ms)?"
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            LocalDateTime timestamp = LocalDateTime.parse(matcher.group("timestamp"), DATE_FORMATTER);
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
     * Converts a RouterLogEntry to a standardized Claim object.
     * @param entry RouterLogEntry
     * @param rawLine Original log line
     * @return Claim
     */
    public Claim toClaim(RouterLogEntry entry, String rawLine) {
        // TODO: Add more robust normalization and confidence scoring
        return Claim.builder()
                .id("claim-" + entry.hashCode())
                .sourceType("ROUTER_LOG")
                .rawData(rawLine)
                .processedData(entry.getSourceIp() + " -> " + entry.getTargetIp())
                .timestamp(java.time.Instant.now())
                .build();
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

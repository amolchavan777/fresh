package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.sources.CiCdEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CiCdAdapter processes CI/CD pipeline logs to extract dependency information.
 * Supports various CI/CD platforms like Jenkins, GitHub Actions, GitLab CI, etc.
 */
@Component
public class CiCdAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CiCdAdapter.class);
    
    // Patterns for different CI/CD log formats
    private static final Pattern JENKINS_PATTERN = Pattern.compile(
        "\\[(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z?)\\]\\s+.*?([\\w-]+)\\s+->\\s+([\\w-]+).*?(DEPLOY|BUILD|TEST|ARTIFACT).*?(SUCCESS|FAILURE|UNSTABLE)"
    );
    
    private static final Pattern GITHUB_ACTIONS_PATTERN = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z?).*?workflow:\\s*([\\w-]+).*?depends_on:\\s*\\[([^\\]]+)\\]"
    );
    
    private static final Pattern GITLAB_CI_PATTERN = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z?).*?stage:\\s*([\\w-]+).*?needs:\\s*\\[([^\\]]+)\\]"
    );
    
    /**
     * Parse CI/CD logs to extract deployment and build dependencies.
     * 
     * @param ciCdLogs Raw CI/CD log data
     * @param format CI/CD platform format (jenkins, github-actions, gitlab-ci, etc.)
     * @return List of claims representing CI/CD dependencies
     */
    public List<Claim> parseCiCdLogs(String ciCdLogs, String format) {
        if (ciCdLogs == null || ciCdLogs.trim().isEmpty()) {
            logger.warn("Empty CI/CD log data provided");
            return new ArrayList<>();
        }
        
        logger.info("Parsing CI/CD logs in {} format", format);
        List<Claim> claims = new ArrayList<>();
        
        String[] lines = ciCdLogs.split("\\n");
        for (String line : lines) {
            try {
                CiCdEvent event = parseLogLine(line.trim(), format);
                if (event != null) {
                    Claim claim = convertToClaim(event);
                    claims.add(claim);
                    logger.debug("Parsed CI/CD event: {}", event);
                }
            } catch (Exception e) {
                logger.debug("Failed to parse CI/CD log line: {}", line, e);
            }
        }
        
        logger.info("Extracted {} CI/CD dependency claims from {} format logs", claims.size(), format);
        return claims;
    }
    
    /**
     * Parse a single log line based on the specified format.
     */
    private CiCdEvent parseLogLine(String line, String format) {
        if (line.isEmpty()) return null;
        
        switch (format.toLowerCase()) {
            case "jenkins":
                return parseJenkinsLog(line);
            case "github-actions":
                return parseGitHubActionsLog(line);
            case "gitlab-ci":
                return parseGitLabCILog(line);
            case "json":
                return parseJsonLog(line);
            default:
                logger.warn("Unsupported CI/CD log format: {}", format);
                return null;
        }
    }
    
    private CiCdEvent parseJenkinsLog(String line) {
        Matcher matcher = JENKINS_PATTERN.matcher(line);
        if (matcher.find()) {
            return CiCdEvent.builder()
                .timestamp(parseTimestamp(matcher.group(1)))
                .sourceStage(matcher.group(2))
                .targetStage(matcher.group(3))
                .action(matcher.group(4))
                .status(matcher.group(5))
                .platform("jenkins")
                .rawLine(line)
                .build();
        }
        return null;
    }
    
    private CiCdEvent parseGitHubActionsLog(String line) {
        Matcher matcher = GITHUB_ACTIONS_PATTERN.matcher(line);
        if (matcher.find()) {
            String dependencies = matcher.group(3);
            // For simplicity, take the first dependency
            String[] deps = dependencies.split(",");
            if (deps.length > 0) {
                return CiCdEvent.builder()
                    .timestamp(parseTimestamp(matcher.group(1)))
                    .sourceStage(deps[0].trim().replaceAll("[\"']", ""))
                    .targetStage(matcher.group(2))
                    .action("WORKFLOW")
                    .status("RUNNING")
                    .platform("github-actions")
                    .rawLine(line)
                    .build();
            }
        }
        return null;
    }
    
    private CiCdEvent parseGitLabCILog(String line) {
        Matcher matcher = GITLAB_CI_PATTERN.matcher(line);
        if (matcher.find()) {
            String dependencies = matcher.group(3);
            String[] deps = dependencies.split(",");
            if (deps.length > 0) {
                return CiCdEvent.builder()
                    .timestamp(parseTimestamp(matcher.group(1)))
                    .sourceStage(deps[0].trim().replaceAll("[\"']", ""))
                    .targetStage(matcher.group(2))
                    .action("STAGE")
                    .status("PENDING")
                    .platform("gitlab-ci")
                    .rawLine(line)
                    .build();
            }
        }
        return null;
    }
    
    private CiCdEvent parseJsonLog(String line) {
        // Simple JSON parsing - in a real implementation, use Jackson
        if (line.contains("timestamp") && line.contains("source") && line.contains("target")) {
            return CiCdEvent.builder()
                .timestamp(Instant.now())
                .sourceStage("unknown-source")
                .targetStage("unknown-target")
                .action("PIPELINE")
                .status("UNKNOWN")
                .platform("json")
                .rawLine(line)
                .build();
        }
        return null;
    }
    
    private Instant parseTimestamp(String timestampStr) {
        try {
            return Instant.parse(timestampStr);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse timestamp: {}", timestampStr);
            return Instant.now();
        }
    }
    
    /**
     * Convert a CI/CD event to a dependency claim.
     */
    private Claim convertToClaim(CiCdEvent event) {
        String processedData = String.format("%s -> %s", event.getSourceStage(), event.getTargetStage());
        
        return Claim.builder()
            .id("cicd_" + UUID.randomUUID().toString())
            .sourceType("CI_CD")
            .rawData(event.getRawLine())
            .processedData(processedData)
            .timestamp(event.getTimestamp())
            .build();
    }
    
    /**
     * Parse CI/CD data from a list of log entries
     * 
     * @param ciCdData List of CI/CD log entries
     * @return List of claims extracted from CI/CD data
     */
    public List<Claim> parseCiCdData(List<String> ciCdData) {
        List<Claim> claims = new ArrayList<>();
        
        if (ciCdData == null || ciCdData.isEmpty()) {
            logger.warn("No CI/CD data provided for parsing");
            return claims;
        }
        
        logger.info("Parsing {} CI/CD entries", ciCdData.size());
        
        for (String entry : ciCdData) {
            try {
                // Try to parse as generic CI/CD log entry
                List<Claim> entryClaims = parseCiCdLogs(entry, "generic");
                claims.addAll(entryClaims);
            } catch (Exception e) {
                logger.warn("Failed to parse CI/CD entry: {}", entry, e);
            }
        }
        
        logger.info("Successfully parsed {} claims from CI/CD data", claims.size());
        return claims;
    }
    
    /**
     * Parse CI/CD logs from a list of log entries.
     * 
     * @param ciCdLogs List of CI/CD log entries
     * @return List of claims representing CI/CD dependencies
     */
    public List<Claim> parseLogData(List<String> ciCdLogs) {
        if (ciCdLogs == null || ciCdLogs.isEmpty()) {
            logger.warn("Empty CI/CD log data provided");
            return new ArrayList<>();
        }
        
        logger.info("Parsing {} CI/CD log entries", ciCdLogs.size());
        List<Claim> claims = new ArrayList<>();
        
        for (String line : ciCdLogs) {
            try {
                // Try different formats to parse the line
                CiCdEvent event = parseLogLineAuto(line.trim());
                if (event != null) {
                    Claim claim = convertToClaim(event);
                    claims.add(claim);
                    logger.debug("Parsed CI/CD event: {}", event);
                }
            } catch (Exception e) {
                logger.debug("Failed to parse CI/CD log line: {}", line, e);
            }
        }
        
        logger.info("Extracted {} CI/CD dependency claims", claims.size());
        return claims;
    }
    
    /**
     * Auto-detect format and parse a log line.
     */
    private CiCdEvent parseLogLineAuto(String line) {
        if (line.isEmpty()) return null;
        
        // Try Jenkins format first
        CiCdEvent event = parseJenkinsLog(line);
        if (event != null) return event;
        
        // Try GitHub Actions format
        event = parseGitHubActionsLog(line);
        if (event != null) return event;
        
        // Try GitLab CI format
        event = parseGitLabCILog(line);
        if (event != null) return event;
        
        // Try JSON format
        event = parseJsonLog(line);
        if (event != null) return event;
        
        logger.debug("Could not parse CI/CD log line with any known format: {}", line);
        return null;
    }
}

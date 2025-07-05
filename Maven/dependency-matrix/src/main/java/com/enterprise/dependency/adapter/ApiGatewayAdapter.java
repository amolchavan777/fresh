package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.model.sources.ApiGatewayCall;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * ApiGatewayAdapter processes API Gateway logs to extract service-to-service 
 * communication patterns and create dependency claims.
 * 
 * <p>Supported log formats:
 * <ul>
 *   <li>JSON formatted API Gateway logs</li>
 *   <li>Common log format (CLF) entries</li>
 *   <li>AWS API Gateway CloudWatch logs</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   ApiGatewayAdapter adapter = new ApiGatewayAdapter();
 *   String logData = readApiGatewayLogs();
 *   List&lt;Claim&gt; claims = adapter.parseApiCalls(logData, "json");
 * </pre>
 */
@Component
public class ApiGatewayAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayAdapter.class);
    
    private final ObjectMapper objectMapper;
    
    // Common Log Format pattern for API Gateway
    private static final Pattern CLF_PATTERN = Pattern.compile(
        "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)\\]\\s+\"(\\S+)\\s+(\\S+)\\s+\\S+\"\\s+\\d+\\s+\\d+\\s+\"[^\"]*\"\\s+\"([^\"]*)\"\\s+(\\d+)ms"
    );
    
    // Pattern to extract service names from endpoints
    private static final Pattern SERVICE_ENDPOINT_PATTERN = Pattern.compile(
        "^/(?:api/)?(?:v\\d+/)?([a-zA-Z][a-zA-Z0-9-]*)"
    );
    
    public ApiGatewayAdapter() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Parses API Gateway logs to extract service call patterns.
     * 
     * @param logData The raw log data from API Gateway
     * @param format The format of the log data ("json", "clf", "aws-cloudwatch")
     * @return List of dependency claims
     */
    public List<Claim> parseApiCalls(String logData, String format) {
        if (logData == null || logData.trim().isEmpty()) {
            logger.warn("Empty log data provided for API Gateway parsing");
            return new ArrayList<>();
        }
        
        logger.info("Parsing API Gateway logs in {} format", format);
        
        List<Claim> claims = new ArrayList<>();
        
        switch (format.toLowerCase()) {
            case "json":
                claims.addAll(parseJsonLogs(logData));
                break;
            case "clf":
                claims.addAll(parseClfLogs(logData));
                break;
            case "aws-cloudwatch":
                claims.addAll(parseAwsCloudWatchLogs(logData));
                break;
            default:
                logger.warn("Unsupported API Gateway log format: {}", format);
        }
        
        logger.info("Extracted {} API call claims from {} format logs", claims.size(), format);
        
        return claims;
    }
    
    /**
     * Parses JSON formatted API Gateway logs.
     */
    private List<Claim> parseJsonLogs(String jsonData) {
        List<Claim> claims = new ArrayList<>();
        
        try {
            // Handle both single JSON object and newline-delimited JSON
            String[] jsonLines = jsonData.split("\\r?\\n");
            
            for (String line : jsonLines) {
                line = line.trim();
                if (line.isEmpty() || !line.startsWith("{")) continue;
                
                processJsonLine(line, claims);
            }
        } catch (Exception e) {
            logger.error("Error parsing JSON API Gateway logs", e);
        }
        
        return claims;
    }
    
    /**
     * Processes a single JSON log line and adds claims if valid.
     */
    private void processJsonLine(String line, List<Claim> claims) {
        try {
            JsonNode logEntry = objectMapper.readTree(line);
            ApiGatewayCall call = parseJsonLogEntry(logEntry);
            if (call != null) {
                Claim claim = createClaimFromApiCall(call);
                claims.add(claim);
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse JSON log entry: {}", line, e);
        }
    }
    
    /**
     * Parses a single JSON log entry into an ApiGatewayCall.
     */
    private ApiGatewayCall parseJsonLogEntry(JsonNode logEntry) {
        try {
            // Extract common fields from different JSON log formats
            Instant timestamp = parseTimestamp(logEntry);
            String method = extractJsonField(logEntry, "method", "httpMethod", "requestMethod");
            String endpoint = extractJsonField(logEntry, "path", "resource", "endpoint");
            String sourceService = extractJsonField(logEntry, "sourceService", "clientId", "userAgent");
            Integer responseTime = extractResponseTime(logEntry);
            
            // Check if timestamp was actually found in the log entry (not just defaulted)
            boolean hasTimestamp = hasAnyField(logEntry, "timestamp", "@timestamp", "time", "eventTime");
            
            // Require at minimum: method, endpoint, and timestamp for high-quality claims
            if (method == null || endpoint == null || !hasTimestamp) {
                logger.debug("Incomplete log entry (missing method, endpoint, or timestamp), skipping: {}", logEntry);
                return null;
            }
            
            String targetService = extractTargetServiceFromEndpoint(endpoint);
            if (sourceService == null) {
                sourceService = "unknown-client";
            }
            
            return ApiGatewayCall.builder()
                .timestamp(timestamp)
                .sourceService(sourceService)
                .targetService(targetService)
                .endpoint(endpoint)
                .method(method.toUpperCase())
                .responseTime(responseTime)
                .build();
                
        } catch (Exception e) {
            logger.warn("Error parsing JSON log entry", e);
            return null;
        }
    }
    
    /**
     * Parses Common Log Format (CLF) API Gateway logs.
     */
    private List<Claim> parseClfLogs(String clfData) {
        List<Claim> claims = new ArrayList<>();
        
        String[] lines = clfData.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            Matcher matcher = CLF_PATTERN.matcher(line);
            if (matcher.matches()) {
                try {
                    String clientIp = matcher.group(1);
                    String timestampStr = matcher.group(2);
                    String method = matcher.group(3);
                    String endpoint = matcher.group(4);
                    String userAgent = matcher.group(5);
                    String responseTimeStr = matcher.group(6);
                    
                    Instant timestamp = parseClfTimestamp(timestampStr);
                    String targetService = extractTargetServiceFromEndpoint(endpoint);
                    String sourceService = deriveSourceFromUserAgent(userAgent, clientIp);
                    Integer responseTime = Integer.parseInt(responseTimeStr);
                    
                    ApiGatewayCall call = ApiGatewayCall.builder()
                        .timestamp(timestamp)
                        .sourceService(sourceService)
                        .targetService(targetService)
                        .endpoint(endpoint)
                        .method(method.toUpperCase())
                        .responseTime(responseTime)
                        .build();
                    
                    Claim claim = createClaimFromApiCall(call);
                    claims.add(claim);
                    
                } catch (Exception e) {
                    logger.warn("Error parsing CLF log line: {}", line, e);
                }
            }
        }
        
        return claims;
    }
    
    /**
     * Parses AWS CloudWatch API Gateway logs.
     */
    private List<Claim> parseAwsCloudWatchLogs(String cloudWatchData) {
        // AWS CloudWatch logs are typically JSON format but with specific fields
        return parseJsonLogs(cloudWatchData);
    }
    
    /**
     * Extracts target service name from API endpoint.
     */
    private String extractTargetServiceFromEndpoint(String endpoint) {
        if (endpoint == null) return "unknown-service";
        
        Matcher matcher = SERVICE_ENDPOINT_PATTERN.matcher(endpoint);
        if (matcher.find()) {
            return matcher.group(1) + "-service";
        }
        
        // Fallback: use first path segment
        String[] segments = endpoint.split("/");
        for (String segment : segments) {
            if (!segment.isEmpty() && !segment.equals("api") && !segment.matches("v\\d+")) {
                return segment + "-service";
            }
        }
        
        return "unknown-service";
    }
    
    /**
     * Creates a Claim from an ApiGatewayCall.
     */
    private Claim createClaimFromApiCall(ApiGatewayCall call) {
        String processedData = String.format("%s -> %s", 
            call.getSourceService(), 
            call.getTargetService());
        
        String rawData = String.format("%s %s [%s] %dms", 
            call.getMethod(), 
            call.getEndpoint(), 
            call.getTimestamp(),
            call.getResponseTime() != null ? call.getResponseTime() : 0);
        
        // Calculate confidence based on response time and endpoint clarity
        double confidence = calculateApiCallConfidence(call);
        
        return Claim.builder()
            .id("apigateway_" + UUID.randomUUID().toString())
            .sourceType("API_GATEWAY")
            .rawData(rawData)
            .processedData(processedData)
            .timestamp(call.getTimestamp())
            .confidenceScore(ConfidenceScore.of(confidence))
            .build();
    }
    
    /**
     * Calculates confidence score for API call claims.
     */
    private double calculateApiCallConfidence(ApiGatewayCall call) {
        double confidence = 0.7; // Base confidence for API Gateway logs
        
        // Higher confidence for well-structured endpoints
        if (call.getEndpoint().matches("^/api/v\\d+/[a-zA-Z][a-zA-Z0-9-]*.*")) {
            confidence += 0.15;
        }
        
        // Higher confidence for standard HTTP methods
        if (call.getMethod().matches("GET|POST|PUT|DELETE|PATCH")) {
            confidence += 0.1;
        }
        
        // Lower confidence for very fast responses (might be cached/errors)
        if (call.getResponseTime() != null && call.getResponseTime() < 10) {
            confidence -= 0.1;
        }
        
        // Higher confidence for reasonable response times
        if (call.getResponseTime() != null && call.getResponseTime() > 10 && call.getResponseTime() < 5000) {
            confidence += 0.05;
        }
        
        // Lower confidence for very slow responses (might indicate issues)
        if (call.getResponseTime() != null && call.getResponseTime() > 10000) {
            confidence -= 0.2;
        }
        
        return Math.min(0.95, Math.max(0.1, confidence));
    }
    
    // Helper methods for parsing various timestamp and field formats
    
    private Instant parseTimestamp(JsonNode logEntry) {
        String[] timestampFields = {"timestamp", "@timestamp", "time", "eventTime"};
        
        for (String field : timestampFields) {
            JsonNode timestampNode = logEntry.get(field);
            if (timestampNode != null) {
                try {
                    if (timestampNode.isNumber()) {
                        return Instant.ofEpochMilli(timestampNode.asLong());
                    } else {
                        return Instant.parse(timestampNode.asText());
                    }
                } catch (DateTimeParseException e) {
                    logger.debug("Could not parse timestamp field {}: {}", field, timestampNode.asText());
                }
            }
        }
        
        return Instant.now(); // Fallback to current time
    }
    
    private boolean hasAnyField(JsonNode logEntry, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = logEntry.get(fieldName);
            if (field != null && !field.isNull()) {
                return true;
            }
        }
        return false;
    }
    
    private String extractJsonField(JsonNode logEntry, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = logEntry.get(fieldName);
            if (field != null && !field.isNull()) {
                return field.asText();
            }
        }
        return null;
    }
    
    private Integer extractResponseTime(JsonNode logEntry) {
        String[] responseTimeFields = {"responseTime", "duration", "latency", "processingTime"};
        
        for (String field : responseTimeFields) {
            JsonNode timeNode = logEntry.get(field);
            if (timeNode != null && timeNode.isNumber()) {
                return timeNode.asInt();
            }
        }
        return null;
    }
    
    private Instant parseClfTimestamp(String timestampStr) {
        try {
            // Common Log Format: [dd/MMM/yyyy:HH:mm:ss Z]
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
            return Instant.from(formatter.parse(timestampStr));
        } catch (DateTimeParseException e) {
            logger.debug("Could not parse CLF timestamp: {}", timestampStr);
            return Instant.now();
        }
    }
    
    private String deriveSourceFromUserAgent(String userAgent, String clientIp) {
        if (userAgent != null && !userAgent.equals("-")) {
            // First try to extract full service names with dashes/underscores
            Pattern servicePattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9-_]*(?:client|service)[a-zA-Z0-9-_]*)");
            Matcher matcher = servicePattern.matcher(userAgent);
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Then try to extract any word ending with 'service' or 'client'
            Pattern wordPattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9-_]*(?:service|client))");
            matcher = wordPattern.matcher(userAgent);
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Extract service name from patterns like "order-service-proxy/2.0"
            String[] parts = userAgent.split("[\\s/]");
            for (String part : parts) {
                if (part.contains("service") || part.contains("client")) {
                    return part;
                }
            }
        }
        
        // Fallback to client IP
        return "client-" + (clientIp != null ? clientIp.replace(".", "-") : "unknown");
    }
}

package com.enterprise.dependency.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LogAndCodebaseParserService provides specialized parsing capabilities for multiple data sources
 * used in dependency discovery and analysis.
 * 
 * <p>This service acts as the primary data ingestion layer, capable of extracting dependency
 * information from various sources including network infrastructure logs and application codebases.</p>
 * 
 * <p><strong>Supported Data Sources:</strong></p>
 * <ul>
 *   <li><strong>Router Logs:</strong> Network router logs showing traffic patterns between services</li>
 *   <li><strong>Network Monitoring Logs:</strong> Network monitoring data showing connection attempts</li>
 *   <li><strong>Java Codebases:</strong> Static analysis of Java source code for dependency discovery</li>
 * </ul>
 * 
 * <p><strong>Parser Features:</strong></p>
 * <ul>
 *   <li>Robust regex-based parsing with error resilience</li>
 *   <li>Comprehensive error collection and reporting</li>
 *   <li>File system traversal for codebase analysis</li>
 *   <li>Structured data extraction from unstructured logs</li>
 * </ul>
 * 
 * <p><strong>Log Format Specifications:</strong></p>
 * 
 * <p><em>Router Log Format:</em></p>
 * <pre>
 * timestamp router action src=source_ip dst=dest_ip proto=protocol dport=dest_port
 * Example: 2025-01-01T10:00:00Z router01 ACCEPT src=10.0.1.100 dst=10.0.2.200 proto=TCP dport=8080
 * </pre>
 * 
 * <p><em>Network Log Format:</em></p>
 * <pre>
 * timestamp monitor action src=source_ip dst=dest_ip status=connection_status
 * Example: 2025-01-01T10:00:00Z monitor01 CONNECT src=10.0.1.100 dst=10.0.2.200 status=SUCCESS
 * </pre>
 * 
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li>Malformed lines are logged but don't stop processing</li>
 *   <li>All errors are collected and reported at the end of parsing</li>
 *   <li>Partial results are returned even if some data fails to parse</li>
 * </ul>
 * 
 * @author Enterprise Architecture Team
 * @version 1.0
 * @since 2025-07-05
 * @see Pattern
 * @see Files
 */
public class LogAndCodebaseParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAndCodebaseParserService.class);
    
    /**
     * Compiled regex pattern for parsing router log entries.
     * 
     * <p>Captures the following named groups:</p>
     * <ul>
     *   <li><strong>timestamp:</strong> ISO 8601 timestamp of the log entry</li>
     *   <li><strong>router:</strong> Router identifier generating the log</li>
     *   <li><strong>action:</strong> Router action (ACCEPT, DROP, MALFORMED)</li>
     *   <li><strong>src:</strong> Source IP address</li>
     *   <li><strong>dst:</strong> Destination IP address</li>
     *   <li><strong>proto:</strong> Network protocol (TCP, UDP, etc.)</li>
     *   <li><strong>dport:</strong> Destination port number</li>
     * </ul>
     */
    private static final Pattern ROUTER_PATTERN = Pattern.compile(
        "(?<timestamp>\\S+)\\s+(?<router>\\S+)\\s+(?<action>ACCEPT|DROP|MALFORMED)\\s+" +
        "src=(?<src>\\S*)\\s+dst=(?<dst>\\S*)\\s+proto=(?<proto>\\S*)\\s+dport=(?<dport>\\S*)"
    );
    
    /**
     * Compiled regex pattern for parsing network monitoring log entries.
     * 
     * <p>Captures the following named groups:</p>
     * <ul>
     *   <li><strong>timestamp:</strong> ISO 8601 timestamp of the monitoring event</li>
     *   <li><strong>monitor:</strong> Monitor identifier generating the log</li>
     *   <li><strong>action:</strong> Monitoring action (CONNECT, MALFORMED)</li>
     *   <li><strong>src:</strong> Source IP address</li>
     *   <li><strong>dst:</strong> Destination IP address</li>
     *   <li><strong>status:</strong> Connection status (SUCCESS, FAILED, TIMEOUT)</li>
     * </ul>
     */
    private static final Pattern NETWORK_PATTERN = Pattern.compile(
        "(?<timestamp>\\S+)\\s+(?<monitor>\\S+)\\s+(?<action>CONNECT|MALFORMED)\\s+" +
        "src=(?<src>\\S*)\\s+dst=(?<dst>\\S*)\\s+status=(?<status>\\S*)"
    );

    /**
     * Parses router log files to extract network traffic patterns and connection data.
     * 
     * <p>Router logs provide valuable insights into network traffic patterns between services,
     * helping to identify runtime dependencies through observed network connections.</p>
     * 
     * <p><strong>Expected Log Format:</strong></p>
     * <pre>
     * timestamp router action src=source_ip dst=dest_ip proto=protocol dport=dest_port
     * </pre>
     * 
     * <p><strong>Parsing Strategy:</strong></p>
     * <ul>
     *   <li>Reads the entire log file line by line</li>
     *   <li>Applies regex pattern matching to extract structured data</li>
     *   <li>Collects parsing errors without stopping processing</li>
     *   <li>Returns all successfully parsed entries</li>
     * </ul>
     * 
     * @param logPath Path to the router log file to parse. Must be readable and exist.
     * @return List of maps containing parsed router log entries. Each map contains keys:
     *         timestamp, router, action, src, dst, proto, dport
     * @throws IOException if the log file cannot be read or accessed
     * @throws IllegalArgumentException if logPath is null
     * 
     * @see #ROUTER_PATTERN
     */
    public List<Map<String, String>> parseRouterLog(Path logPath) throws IOException {
        Objects.requireNonNull(logPath, "Log path cannot be null");
        
        logger.info("Starting router log parsing for file: {}", logPath);
        
        List<Map<String, String>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;
        
        // Read all lines from the log file
        List<String> lines = Files.readAllLines(logPath);
        logger.debug("Read {} lines from router log file", lines.size());
        
        for (String line : lines) {
            lineNumber++;
            try {
                Matcher matcher = ROUTER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    // Extract all named groups from the regex match
                    Map<String, String> entry = new HashMap<>();
                    String[] groups = {"timestamp", "router", "action", "src", "dst", "proto", "dport"};
                    
                    for (String group : groups) {
                        String value = matcher.group(group);
                        entry.put(group, value != null ? value : "");
                    }
                    
                    results.add(entry);
                    logger.trace("Successfully parsed router log entry at line {}: {}", lineNumber, entry);
                } else {
                    String errorMsg = String.format("Malformed router log at line %d: %s", lineNumber, line);
                    errors.add(errorMsg);
                    logger.debug(errorMsg);
                }
            } catch (Exception e) {
                String errorMsg = String.format("Error processing line %d: %s - %s", lineNumber, line, e.getMessage());
                errors.add(errorMsg);
                logger.warn(errorMsg, e);
            }
        }
        
        // Report parsing results
        if (!errors.isEmpty()) {
            logger.warn("Router log parsing completed with {} errors out of {} total lines", 
                errors.size(), lineNumber);
            logger.debug("Router log parse errors: {}", errors);
        } else {
            logger.info("Router log parsing completed successfully: {} entries parsed from {} lines", 
                results.size(), lineNumber);
        }
        
        return results;
    }

    /**
     * Parses network monitoring log files to extract connection attempt data.
     * 
     * <p>Network monitoring logs capture connection attempts and their outcomes,
     * providing insights into which services are trying to communicate with each other.</p>
     * 
     * <p><strong>Expected Log Format:</strong></p>
     * <pre>
     * timestamp monitor action src=source_ip dst=dest_ip status=connection_status
     * </pre>
     * 
     * @param logPath Path to the network monitoring log file to parse. Must be readable and exist.
     * @return List of maps containing parsed network log entries. Each map contains keys:
     *         timestamp, monitor, action, src, dst, status
     * @throws IOException if the log file cannot be read or accessed
     * @throws IllegalArgumentException if logPath is null
     * 
     * @see #NETWORK_PATTERN
     */
    public List<Map<String, String>> parseNetworkLog(Path logPath) throws IOException {
        Objects.requireNonNull(logPath, "Log path cannot be null");
        
        logger.info("Starting network log parsing for file: {}", logPath);
        
        List<Map<String, String>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;
        
        List<String> lines = Files.readAllLines(logPath);
        logger.debug("Read {} lines from network log file", lines.size());
        
        for (String line : lines) {
            lineNumber++;
            try {
                Matcher matcher = NETWORK_PATTERN.matcher(line);
                if (matcher.matches()) {
                    Map<String, String> entry = new HashMap<>();
                    String[] groups = {"timestamp", "monitor", "action", "src", "dst", "status"};
                    
                    for (String group : groups) {
                        String value = matcher.group(group);
                        entry.put(group, value != null ? value : "");
                    }
                    
                    results.add(entry);
                    logger.trace("Successfully parsed network log entry at line {}: {}", lineNumber, entry);
                } else {
                    String errorMsg = String.format("Malformed network log at line %d: %s", lineNumber, line);
                    errors.add(errorMsg);
                    logger.debug(errorMsg);
                }
            } catch (Exception e) {
                String errorMsg = String.format("Error processing line %d: %s - %s", lineNumber, line, e.getMessage());
                errors.add(errorMsg);
                logger.warn(errorMsg, e);
            }
        }
        
        if (!errors.isEmpty()) {
            logger.warn("Network log parsing completed with {} errors out of {} total lines", 
                errors.size(), lineNumber);
            logger.debug("Network log parse errors: {}", errors);
        } else {
            logger.info("Network log parsing completed successfully: {} entries parsed from {} lines", 
                results.size(), lineNumber);
        }
        
        return results;
    }

    /**
     * Performs static analysis of Java codebases to discover dependencies through constructor calls.
     * 
     * <p>This method analyzes Java source files to identify potential dependencies by looking for
     * constructor invocations (new ClassName()) which often indicate direct dependencies between classes.</p>
     * 
     * <p><strong>Analysis Approach:</strong></p>
     * <ul>
     *   <li>Recursively traverses the codebase directory structure</li>
     *   <li>Processes all Java source files (.java extension)</li>
     *   <li>Uses regex pattern matching to find constructor invocations</li>
     *   <li>Maps class names to their discovered dependencies</li>
     * </ul>
     * 
     * <p><strong>Limitations:</strong></p>
     * <ul>
     *   <li>Only detects direct constructor calls (new ClassName())</li>
     *   <li>Does not analyze method calls, field access, or static usage</li>
     *   <li>Does not resolve package imports or fully qualified names</li>
     *   <li>Basic regex parsing - not a full AST analysis</li>
     * </ul>
     * 
     * <p><strong>Future Enhancements:</strong></p>
     * <ul>
     *   <li>Full AST parsing using JavaParser or similar library</li>
     *   <li>Import resolution for better dependency accuracy</li>
     *   <li>Method call analysis for runtime dependencies</li>
     *   <li>Annotation-based dependency discovery</li>
     * </ul>
     * 
     * @param codebaseDir Root directory of the Java codebase to analyze. Must be readable and exist.
     * @return Map where keys are class names and values are sets of dependency class names
     * @throws IOException if the directory cannot be accessed or files cannot be read
     * @throws IllegalArgumentException if codebaseDir is null
     */
    public Map<String, Set<String>> parseJavaCodebase(Path codebaseDir) throws IOException {
        Objects.requireNonNull(codebaseDir, "Codebase directory cannot be null");
        
        logger.info("Starting Java codebase analysis for directory: {}", codebaseDir);
        
        Map<String, Set<String>> dependencies = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int filesProcessed = 0;
        
        // Use try-with-resources to ensure the stream is properly closed
        try (Stream<Path> javaFiles = Files.walk(codebaseDir)
                .filter(path -> path.toString().endsWith(".java"))) {
            
            javaFiles.forEach(javaFile -> {
                try {
                    logger.debug("Analyzing Java file: {}", javaFile);
                    
                    // Read the entire file content
                    String content = new String(Files.readAllBytes(javaFile));
                    
                    // Extract class name from file name
                    String className = javaFile.getFileName().toString().replace(".java", "");
                    
                    // Find all constructor calls using regex
                    Set<String> constructorCalls = new HashSet<>();
                    Pattern constructorPattern = Pattern.compile("new\\s+(\\w+)\\s*\\(");
                    Matcher matcher = constructorPattern.matcher(content);
                    
                    while (matcher.find()) {
                        String dependencyClass = matcher.group(1);
                        // Filter out primitive types and common Java types that aren't dependencies
                        if (!isPrimitiveOrCommonType(dependencyClass)) {
                            constructorCalls.add(dependencyClass);
                            logger.trace("Found dependency: {} -> {}", className, dependencyClass);
                        }
                    }
                    
                    dependencies.put(className, constructorCalls);
                    logger.debug("Class {} has {} dependencies: {}", className, constructorCalls.size(), constructorCalls);
                    
                } catch (Exception e) {
                    String errorMsg = String.format("Codebase parse error in %s: %s", javaFile, e.getMessage());
                    errors.add(errorMsg);
                    logger.warn(errorMsg, e);
                }
            });
            
            filesProcessed = dependencies.size();
        }
        
        if (!errors.isEmpty()) {
            logger.warn("Codebase analysis completed with {} errors out of {} files processed", 
                errors.size(), filesProcessed);
            logger.debug("Codebase parse errors: {}", errors);
        } else {
            logger.info("Codebase analysis completed successfully: {} files processed, {} classes analyzed", 
                filesProcessed, dependencies.size());
        }
        
        return dependencies;
    }
    
    /**
     * Helper method to filter out primitive types and common Java types that aren't meaningful dependencies.
     * 
     * @param className The class name to check
     * @return true if this is a primitive or common type that should be filtered out
     */
    private boolean isPrimitiveOrCommonType(String className) {
        // Filter out common Java types that aren't meaningful dependencies
        Set<String> commonTypes = Set.of(
            "String", "Integer", "Long", "Double", "Float", "Boolean", "Character",
            "Object", "List", "Map", "Set", "Collection", "ArrayList", "HashMap", "HashSet",
            "Date", "LocalDate", "LocalDateTime", "Instant", "BigDecimal", "BigInteger",
            "StringBuilder", "StringBuffer", "Exception", "RuntimeException"
        );
        
        return commonTypes.contains(className);
    }
}

@Configuration
class ParserServiceConfig {
    @Bean
    public LogAndCodebaseParserService logAndCodebaseParserService() {
        return new LogAndCodebaseParserService();
    }
}

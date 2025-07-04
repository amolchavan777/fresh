package com.enterprise.dependency.web;

import com.enterprise.dependency.service.LogAndCodebaseParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.*;

/**
 * ParserController provides RESTful API endpoints for triggering various parsing operations
 * in the Application Dependency Matrix system.
 * 
 * <p>This controller serves as the primary interface for external systems and users to
 * initiate data parsing from different sources including logs and codebases.</p>
 * 
 * <p><strong>Available Endpoints:</strong></p>
 * <ul>
 *   <li><strong>GET /api/parse/router-logs</strong> - Parse router log files</li>
 *   <li><strong>GET /api/parse/network-logs</strong> - Parse network monitoring logs</li>
 *   <li><strong>GET /api/parse/codebase</strong> - Analyze Java codebase for dependencies</li>
 * </ul>
 * 
 * <p><strong>Response Format:</strong></p>
 * <ul>
 *   <li><strong>Success:</strong> HTTP 200 with parsed data in JSON format</li>
 *   <li><strong>Error:</strong> HTTP 400 with error details in JSON format</li>
 * </ul>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <p>All endpoints implement comprehensive error handling that catches exceptions
 * and returns structured error responses to clients. Detailed error information
 * is logged for troubleshooting purposes.</p>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * # Parse router logs
 * curl -X GET http://localhost:8080/api/parse/router-logs
 * 
 * # Parse network logs
 * curl -X GET http://localhost:8080/api/parse/network-logs
 * 
 * # Analyze codebase
 * curl -X GET http://localhost:8080/api/parse/codebase
 * </pre>
 * 
 * @author Enterprise Architecture Team
 * @version 1.0
 * @since 2025-07-05
 * @see LogAndCodebaseParserService
 */
@RestController
@RequestMapping("/api/parse")
public class ParserController {
    
    private static final Logger logger = LoggerFactory.getLogger(ParserController.class);
    
    /** Error response key constant to avoid duplication */
    private static final String ERROR_KEY = "error";
    
    /** Service for parsing logs and codebase files */
    private final LogAndCodebaseParserService parserService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param parserService The parsing service to inject
     */
    @Autowired
    public ParserController(LogAndCodebaseParserService parserService) {
        this.parserService = Objects.requireNonNull(parserService, "ParserService cannot be null");
        logger.info("ParserController initialized successfully");
    }

    /**
     * Parses router log files and returns structured network traffic data.
     * 
     * <p>This endpoint triggers the parsing of router logs located in the standard
     * resources directory. Router logs contain network traffic information that
     * helps identify runtime dependencies between services.</p>
     * 
     * <p><strong>Expected Log Location:</strong> src/main/resources/logs/router.log</p>
     * 
     * <p><strong>Response Structure:</strong></p>
     * <pre>
     * Success (HTTP 200):
     * [
     *   {
     *     "timestamp": "2025-01-01T10:00:00Z",
     *     "router": "router01",
     *     "action": "ACCEPT",
     *     "src": "10.0.1.100",
     *     "dst": "10.0.2.200",
     *     "proto": "TCP",
     *     "dport": "8080"
     *   }
     * ]
     * 
     * Error (HTTP 400):
     * {
     *   "error": "Detailed error message"
     * }
     * </pre>
     * 
     * @return ResponseEntity containing parsed router log data or error information
     */
    @GetMapping("/router-logs")
    public ResponseEntity<Object> parseRouterLogs() {
        logger.info("Received request to parse router logs");
        
        try {
            Object result = parserService.parseRouterLog(Paths.get("src/main/resources/logs/router.log"));
            logger.info("Router log parsing completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to parse router logs", e);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    /**
     * Parses network monitoring log files and returns connection attempt data.
     * 
     * <p>This endpoint triggers the parsing of network monitoring logs that capture
     * connection attempts and their outcomes between services.</p>
     * 
     * <p><strong>Expected Log Location:</strong> src/main/resources/logs/network.log</p>
     * 
     * @return ResponseEntity containing parsed network log data or error information
     */
    @GetMapping("/network-logs")
    public ResponseEntity<Object> parseNetworkLogs() {
        logger.info("Received request to parse network logs");
        
        try {
            Object result = parserService.parseNetworkLog(Paths.get("src/main/resources/logs/network.log"));
            logger.info("Network log parsing completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to parse network logs", e);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    /**
     * Analyzes Java codebase and returns discovered dependencies through static analysis.
     * 
     * <p>This endpoint performs static analysis of Java source files to identify
     * dependencies through constructor invocations and other code patterns.</p>
     * 
     * <p><strong>Expected Codebase Location:</strong> src/main/resources/codebases/</p>
     * 
     * <p><strong>Response Structure:</strong></p>
     * <pre>
     * Success (HTTP 200):
     * {
     *   "ServiceA": ["ServiceB", "ServiceC"],
     *   "ServiceB": ["ServiceD"],
     *   "ServiceC": []
     * }
     * 
     * Error (HTTP 400):
     * {
     *   "error": "Detailed error message"
     * }
     * </pre>
     * 
     * @return ResponseEntity containing discovered dependencies map or error information
     */
    @GetMapping("/codebase")
    public ResponseEntity<Object> parseCodebase() {
        logger.info("Received request to analyze codebase");
        
        try {
            Object result = parserService.parseJavaCodebase(Paths.get("src/main/resources/codebases/"));
            logger.info("Codebase analysis completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to analyze codebase", e);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }
}

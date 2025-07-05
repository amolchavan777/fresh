package com.enterprise.dependency.service;

import com.enterprise.dependency.adapter.ApiGatewayAdapter;
import com.enterprise.dependency.adapter.CodebaseAdapter;
import com.enterprise.dependency.adapter.RouterLogAdapter;
import com.enterprise.dependency.engine.ClaimProcessingEngine;
import com.enterprise.dependency.engine.ConflictResolutionEngine;
import com.enterprise.dependency.engine.DependencyGraphBuilder;
import com.enterprise.dependency.engine.InferenceEngine;
import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.Dependency;
import com.enterprise.dependency.model.core.DependencyMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DependencyMatrixService orchestrates the complete pipeline:
 * Raw Data → Claims → Scoring → Conflict Resolution → Inference → Dependency Matrix
 * 
 * This is the main integration service that demonstrates the full end-to-end workflow.
 */
@Service
public class DependencyMatrixService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyMatrixService.class);
    
    private final RouterLogAdapter routerLogAdapter;
    private final CodebaseAdapter codebaseAdapter;
    private final ApiGatewayAdapter apiGatewayAdapter;
    private final ClaimProcessingEngine claimProcessingEngine;
    private final ConflictResolutionEngine conflictResolutionEngine;
    private final InferenceEngine inferenceEngine;
    private final DependencyGraphBuilder graphBuilder;
    
    @Autowired
    public DependencyMatrixService(
            RouterLogAdapter routerLogAdapter,
            CodebaseAdapter codebaseAdapter,
            ApiGatewayAdapter apiGatewayAdapter,
            ClaimProcessingEngine claimProcessingEngine,
            ConflictResolutionEngine conflictResolutionEngine,
            InferenceEngine inferenceEngine,
            DependencyGraphBuilder graphBuilder) {
        this.routerLogAdapter = routerLogAdapter;
        this.codebaseAdapter = codebaseAdapter;
        this.apiGatewayAdapter = apiGatewayAdapter;
        this.claimProcessingEngine = claimProcessingEngine;
        this.conflictResolutionEngine = conflictResolutionEngine;
        this.inferenceEngine = inferenceEngine;
        this.graphBuilder = graphBuilder;
    }
    
    /**
     * Complete end-to-end processing pipeline.
     * 
     * @param routerLogs Router log data as raw strings
     * @param codebaseDeps Codebase dependency data as raw strings
     * @param apiGatewayLogs API Gateway log data as raw strings
     * @return Complete dependency matrix with metrics
     */
    public DependencyMatrix processFullPipeline(
            List<String> routerLogs,
            List<String> codebaseDeps,
            List<String> apiGatewayLogs) {
        
        Instant startTime = Instant.now();
        logger.info("Starting full dependency matrix pipeline processing");
        
        try {
            // Step 1: Data Ingestion - Convert raw data to claims
            List<Claim> allClaims = ingestData(routerLogs, codebaseDeps, apiGatewayLogs);
            logger.info("Step 1 Complete: Ingested {} claims from all sources", allClaims.size());
            
            // Step 2: Claim Processing - Apply confidence scoring
            List<Claim> scoredClaims = processClaims(allClaims);
            logger.info("Step 2 Complete: Scored {} claims", scoredClaims.size());
            
            // Step 3: Conflict Resolution - Resolve conflicting claims
            List<Claim> resolvedClaims = resolveClaims(scoredClaims);
            logger.info("Step 3 Complete: Resolved to {} final claims", resolvedClaims.size());
            
            // Step 4: Dependency Inference - Convert claims to dependencies
            List<Dependency> dependencies = inferDependencies(resolvedClaims);
            logger.info("Step 4 Complete: Inferred {} dependencies", dependencies.size());
            
            // Step 5: Graph Construction - Build dependency graph
            Map<String, Object> dependencyGraph = buildDependencyGraph(dependencies);
            logger.info("Step 5 Complete: Built dependency graph");
            
            // Step 6: Matrix Generation - Create final dependency matrix
            DependencyMatrix matrix = generateDependencyMatrix(dependencies, dependencyGraph, startTime);
            logger.info("Pipeline Complete: Generated dependency matrix with {} applications and {} dependencies", 
                matrix.getApplicationCount(), matrix.getDependencyCount());
            
            return matrix;
            
        } catch (Exception e) {
            logger.error("Pipeline processing failed", e);
            throw new RuntimeException("Failed to process dependency matrix pipeline", e);
        }
    }
    
    /**
     * Step 1: Data Ingestion - Extract claims from all data sources
     */
    private List<Claim> ingestData(List<String> routerLogs, List<String> codebaseDeps, List<String> apiGatewayLogs) {
        List<Claim> allClaims = new ArrayList<>();
        
        // Process router logs
        if (routerLogs != null && !routerLogs.isEmpty()) {
            List<Claim> routerClaims = routerLogAdapter.parseLogData(routerLogs);
            allClaims.addAll(routerClaims);
            logger.debug("Extracted {} claims from router logs", routerClaims.size());
        }
        
        // Process codebase dependencies
        if (codebaseDeps != null && !codebaseDeps.isEmpty()) {
            List<Claim> codebaseClaims = codebaseAdapter.parseCodebaseDependencies(codebaseDeps);
            allClaims.addAll(codebaseClaims);
            logger.debug("Extracted {} claims from codebase dependencies", codebaseClaims.size());
        }
        
        // Process API gateway logs
        if (apiGatewayLogs != null && !apiGatewayLogs.isEmpty()) {
            // Convert list to single string for the existing method
            String combinedLogs = String.join("\n", apiGatewayLogs);
            List<Claim> apiClaims = apiGatewayAdapter.parseApiCalls(combinedLogs, "json");
            allClaims.addAll(apiClaims);
            logger.debug("Extracted {} claims from API gateway logs", apiClaims.size());
        }
        
        return allClaims;
    }
    
    /**
     * Step 2: Claim Processing - Apply confidence scoring to all claims
     */
    private List<Claim> processClaims(List<Claim> rawClaims) {
        return claimProcessingEngine.processClaims(rawClaims);
    }
    
    /**
     * Step 3: Conflict Resolution - Resolve conflicting claims using weighted voting
     */
    private List<Claim> resolveClaims(List<Claim> scoredClaims) {
        return conflictResolutionEngine.resolveClaims(scoredClaims);
    }
    
    /**
     * Step 4: Dependency Inference - Convert resolved claims to dependencies
     */
    private List<Dependency> inferDependencies(List<Claim> resolvedClaims) {
        return inferenceEngine.inferDependencies(resolvedClaims);
    }
    
    /**
     * Step 5: Graph Construction - Build dependency graph structure
     */
    private Map<String, Object> buildDependencyGraph(List<Dependency> dependencies) {
        DependencyGraphBuilder.DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        
        // Convert to Map representation for the matrix
        Map<String, Object> graphMap = new HashMap<>();
        graphMap.put("nodes", graph.getNodes());
        graphMap.put("cycles", graph.detectCycles());
        graphMap.put("hasCycles", !graph.detectCycles().isEmpty());
        graphMap.put("topologicalOrder", graph.getTopologicalOrder());
        
        // Get statistics if available
        try {
            DependencyGraphBuilder.GraphStats stats = graph.getStatistics();
            graphMap.put("nodeCount", stats.getNodeCount());
            graphMap.put("edgeCount", stats.getEdgeCount());
            graphMap.put("cycleCount", stats.getCycleCount());
            graphMap.put("componentCount", stats.getComponentCount());
        } catch (Exception e) {
            // Fallback to basic counts
            graphMap.put("nodeCount", graph.getNodes().size());
            graphMap.put("edgeCount", dependencies.size());
            graphMap.put("cycleCount", graph.detectCycles().size());
            logger.debug("Using fallback statistics calculation", e);
        }
        
        return graphMap;
    }
    
    /**
     * Step 6: Matrix Generation - Create final dependency matrix with metadata
     */
    private DependencyMatrix generateDependencyMatrix(List<Dependency> dependencies, 
                                                     Map<String, Object> graph, 
                                                     Instant startTime) {
        return DependencyMatrix.builder()
            .dependencies(dependencies)
            .graph(graph)
            .applicationCount(extractApplicationCount(graph))
            .dependencyCount(dependencies.size())
            .processingTimeMs(java.time.Duration.between(startTime, Instant.now()).toMillis())
            .timestamp(Instant.now())
            .build();
    }
    
    /**
     * Extract application count from graph structure
     */
    private int extractApplicationCount(Map<String, Object> graph) {
        Object nodeCount = graph.get("nodeCount");
        if (nodeCount instanceof Integer) {
            return (Integer) nodeCount;
        }
        
        Object nodes = graph.get("nodes");
        if (nodes instanceof java.util.Set) {
            return ((java.util.Set<?>) nodes).size();
        }
        return 0;
    }
    
    /**
     * Process sample data for demonstration purposes
     */
    public DependencyMatrix processSampleData() {
        logger.info("Processing sample data for demonstration");
        
        // Sample router logs
        List<String> sampleRouterLogs = List.of(
            "2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 HTTP GET /api/users 200 125ms",
            "2024-07-04 10:30:46 [INFO] 192.168.1.200 -> 192.168.1.150:3306 TCP connection established",
            "2024-07-04 10:30:47 [INFO] 192.168.1.100 -> 192.168.1.201:8080 HTTP POST /api/orders 201 200ms"
        );
        
        // Sample codebase dependencies
        List<String> sampleCodebaseDeps = List.of(
            "maven:com.enterprise:user-service-client:2.1.0",
            "maven:com.enterprise:order-service-client:1.5.0",
            "npm:@company/shared-utils:3.2.1"
        );
        
        // Sample API gateway logs
        List<String> sampleApiLogs = List.of(
            "{\"timestamp\":\"2024-07-04T10:30:45Z\",\"method\":\"GET\",\"path\": \"/api/users\",\"user_agent\":\"web-portal/1.0\",\"response_time\":125}",
            "{\"timestamp\":\"2024-07-04T10:31:00Z\",\"method\":\"POST\",\"path\":\"/api/orders\",\"user_agent\":\"mobile-app/2.1\",\"response_time\":200}",
            "{\"timestamp\":\"2024-07-04T10:31:15Z\",\"method\":\"GET\",\"path\":\"/api/inventory\",\"user_agent\":\"inventory-service/1.2\",\"response_time\":80}"
        );
        
        return processFullPipeline(sampleRouterLogs, sampleCodebaseDeps, sampleApiLogs);
    }
}

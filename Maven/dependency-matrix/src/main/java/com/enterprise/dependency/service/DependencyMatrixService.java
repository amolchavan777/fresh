package com.enterprise.dependency.service;

import com.enterprise.dependency.adapter.ApiGatewayAdapter;
import com.enterprise.dependency.adapter.CodebaseAdapter;
import com.enterprise.dependency.adapter.RouterLogAdapter;
import com.enterprise.dependency.adapter.CiCdAdapter;
import com.enterprise.dependency.adapter.TelemetryAdapter;
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
    private final CiCdAdapter ciCdAdapter;
    private final TelemetryAdapter telemetryAdapter;
    private final ClaimProcessingEngine claimProcessingEngine;
    private final ConflictResolutionEngine conflictResolutionEngine;
    private final InferenceEngine inferenceEngine;
    private final DependencyGraphBuilder graphBuilder;

    @Autowired
    public DependencyMatrixService(
            RouterLogAdapter routerLogAdapter,
            CodebaseAdapter codebaseAdapter,
            ApiGatewayAdapter apiGatewayAdapter,
            CiCdAdapter ciCdAdapter,
            TelemetryAdapter telemetryAdapter,
            ClaimProcessingEngine claimProcessingEngine,
            ConflictResolutionEngine conflictResolutionEngine,
            InferenceEngine inferenceEngine,
            DependencyGraphBuilder graphBuilder) {
        this.routerLogAdapter = routerLogAdapter;
        this.codebaseAdapter = codebaseAdapter;
        this.apiGatewayAdapter = apiGatewayAdapter;
        this.ciCdAdapter = ciCdAdapter;
        this.telemetryAdapter = telemetryAdapter;
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
     * Process sample data for demonstration purposes with comprehensive multi-source data
     */
    public DependencyMatrix processSampleData() {
        logger.info("Processing enhanced sample data for demonstration");
        
        // Enhanced sample router logs - representing a real microservices architecture
        List<String> sampleRouterLogs = List.of(
            // User service interactions
            "2024-07-04T10:30:45Z [INFO] user-service -> auth-service:8080 HTTP GET /api/validate 200 125ms",
            "2024-07-04T10:30:46Z [INFO] user-service -> database:3306 TCP connection established",
            "2024-07-04T10:30:47Z [INFO] api-gateway -> order-service:8080 HTTP POST /api/orders 201 200ms",
            
            // Order service interactions
            "2024-07-04T10:31:00Z [INFO] order-service -> inventory-service:8080 HTTP GET /api/inventory/check 200 80ms",
            "2024-07-04T10:31:05Z [INFO] order-service -> payment-service:8080 HTTP POST /api/payment/process 200 300ms",
            "2024-07-04T10:31:10Z [INFO] order-service -> notification-service:8080 HTTP POST /api/notifications/send 202 50ms",
            
            // Inventory service interactions
            "2024-07-04T10:31:15Z [INFO] inventory-service -> database:3306 TCP connection established",
            "2024-07-04T10:31:20Z [INFO] inventory-service -> redis-cache:6379 REDIS GET inventory:item:123 200 5ms",
            
            // Payment service interactions
            "2024-07-04T10:31:25Z [INFO] payment-service -> external-stripe:443 HTTPS POST /api/external/stripe 200 400ms",
            "2024-07-04T10:31:30Z [INFO] payment-service -> database:3306 TCP connection established",
            
            // Notification service interactions
            "2024-07-04T10:31:35Z [INFO] notification-service -> email-gateway:587 SMTP SEND email notification 250 100ms",
            "2024-07-04T10:31:40Z [INFO] notification-service -> sendgrid-api:443 HTTPS POST /api/external/sendgrid 200 150ms",
            
            // API Gateway to services
            "2024-07-04T10:32:00Z [INFO] api-gateway -> user-service:8080 HTTP GET /api/users/profile 200 90ms",
            "2024-07-04T10:32:05Z [INFO] api-gateway -> order-service:8080 HTTP GET /api/orders/history 200 110ms",
            "2024-07-04T10:32:10Z [INFO] api-gateway -> inventory-service:8080 HTTP GET /api/inventory/status 200 75ms",
            
            // Analytics and monitoring
            "2024-07-04T10:32:15Z [INFO] analytics-service -> kafka-broker:9092 KAFKA PRODUCE events topic 200 25ms",
            "2024-07-04T10:32:20Z [INFO] monitoring-service -> prometheus:9090 HTTP GET /metrics 200 15ms",
            
            // Mobile and web frontends
            "2024-07-04T10:32:25Z [INFO] mobile-app -> api-gateway:8080 HTTP POST /api/mobile/login 200 180ms",
            "2024-07-04T10:32:30Z [INFO] web-portal -> api-gateway:8080 HTTP GET /api/dashboard 200 95ms"
        );
        
        // Enhanced sample codebase dependencies - representing a real project structure
        List<String> sampleCodebaseDeps = List.of(
            // Maven dependencies for Java services
            "<dependency><groupId>com.enterprise</groupId><artifactId>user-service-client</artifactId><version>2.1.0</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>order-service-client</artifactId><version>1.5.0</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>inventory-service-client</artifactId><version>1.8.2</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>payment-service-client</artifactId><version>3.0.1</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>notification-service-client</artifactId><version>1.2.5</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>shared-models</artifactId><version>4.1.0</version></dependency>",
            "<dependency><groupId>com.enterprise</groupId><artifactId>common-utils</artifactId><version>2.3.0</version></dependency>",
            
            // NPM dependencies for frontend services
            "npm:@company/user-api-client:^2.1.0",
            "npm:@company/order-api-client:^1.5.0",
            "npm:@company/shared-components:^3.2.1",
            "npm:@company/design-system:^1.0.5",
            
            // Gradle dependencies for Android/Kotlin services
            "gradle:com.enterprise:mobile-api-client:1.4.2",
            "gradle:com.enterprise:analytics-client:2.0.0",
            
            // Python pip dependencies for ML/Data services
            "pip:enterprise-ml-utils==1.2.3",
            "pip:data-pipeline-client==0.8.1"
        );
        
        // Enhanced sample API gateway logs - representing various client types
        List<String> sampleApiLogs = List.of(
            // Web portal requests
            "{\"timestamp\":\"2024-07-04T10:30:45Z\",\"method\":\"GET\",\"path\":\"/api/users\",\"user_agent\":\"web-portal/1.0\",\"response_time\":125,\"source_ip\":\"192.168.1.100\"}",
            "{\"timestamp\":\"2024-07-04T10:30:50Z\",\"method\":\"POST\",\"path\":\"/api/users/login\",\"user_agent\":\"web-portal/1.0\",\"response_time\":200,\"source_ip\":\"192.168.1.100\"}",
            
            // Mobile app requests
            "{\"timestamp\":\"2024-07-04T10:31:00Z\",\"method\":\"POST\",\"path\":\"/api/orders\",\"user_agent\":\"mobile-app/2.1\",\"response_time\":200,\"source_ip\":\"192.168.1.101\"}",
            "{\"timestamp\":\"2024-07-04T10:31:05Z\",\"method\":\"GET\",\"path\":\"/api/orders/123\",\"user_agent\":\"mobile-app/2.1\",\"response_time\":150,\"source_ip\":\"192.168.1.101\"}",
            
            // Internal service-to-service calls
            "{\"timestamp\":\"2024-07-04T10:31:15Z\",\"method\":\"GET\",\"path\":\"/api/inventory\",\"user_agent\":\"order-service/1.5\",\"response_time\":80,\"source_ip\":\"192.168.1.201\"}",
            "{\"timestamp\":\"2024-07-04T10:31:20Z\",\"method\":\"POST\",\"path\":\"/api/payment/validate\",\"user_agent\":\"order-service/1.5\",\"response_time\":300,\"source_ip\":\"192.168.1.201\"}",
            
            // Admin dashboard requests
            "{\"timestamp\":\"2024-07-04T10:31:30Z\",\"method\":\"GET\",\"path\":\"/api/admin/dashboard\",\"user_agent\":\"admin-portal/1.2\",\"response_time\":95,\"source_ip\":\"192.168.1.102\"}",
            "{\"timestamp\":\"2024-07-04T10:31:35Z\",\"method\":\"GET\",\"path\":\"/api/admin/metrics\",\"user_agent\":\"admin-portal/1.2\",\"response_time\":120,\"source_ip\":\"192.168.1.102\"}",
            
            // Analytics and reporting
            "{\"timestamp\":\"2024-07-04T10:31:40Z\",\"method\":\"POST\",\"path\":\"/api/analytics/events\",\"user_agent\":\"analytics-service/2.0\",\"response_time\":45,\"source_ip\":\"192.168.1.210\"}",
            "{\"timestamp\":\"2024-07-04T10:31:45Z\",\"method\":\"GET\",\"path\":\"/api/reports/sales\",\"user_agent\":\"reporting-service/1.1\",\"response_time\":500,\"source_ip\":\"192.168.1.211\"}",
            
            // Health checks and monitoring
            "{\"timestamp\":\"2024-07-04T10:32:00Z\",\"method\":\"GET\",\"path\":\"/health\",\"user_agent\":\"monitoring-agent/1.0\",\"response_time\":15,\"source_ip\":\"192.168.1.220\"}",
            "{\"timestamp\":\"2024-07-04T10:32:05Z\",\"method\":\"GET\",\"path\":\"/metrics\",\"user_agent\":\"prometheus/2.0\",\"response_time\":25,\"source_ip\":\"192.168.1.221\"}"
        );
        
        // Enhanced sample CI/CD logs - representing deployment dependencies
        List<String> sampleCiCdLogs = List.of(
            // Jenkins deployment pipeline
            "[2024-07-04T10:30:00.123Z] Job: deploy-user-service -> deploy-database SUCCESS",
            "[2024-07-04T10:30:30.456Z] Job: deploy-order-service -> deploy-user-service SUCCESS",
            "[2024-07-04T10:31:00.789Z] Job: deploy-payment-service -> deploy-order-service SUCCESS",
            
            // GitHub Actions workflow dependencies
            "2024-07-04T10:31:30.000Z workflow: api-gateway-deployment depends_on: [user-service, order-service]",
            "2024-07-04T10:32:00.000Z workflow: frontend-deployment depends_on: [api-gateway-deployment]",
            
            // GitLab CI pipeline stages
            "2024-07-04T10:32:30.000Z stage: integration-tests needs: [unit-tests, build-services]",
            "2024-07-04T10:33:00.000Z stage: deploy-production needs: [integration-tests, security-scan]"
        );
        
        // Enhanced sample telemetry data - representing observability and monitoring
        List<String> sampleTelemetryLogs = List.of(
            // Prometheus HTTP request metrics
            "http_requests_total{job=\"user-service\",instance=\"user-service:8080\",target=\"auth-service\"} 1250.5",
            "http_requests_total{job=\"order-service\",instance=\"order-service:8080\",target=\"inventory-service\"} 890.2",
            "http_requests_total{job=\"payment-service\",instance=\"payment-service:8080\",target=\"external-stripe\"} 324.7",
            
            // OpenTelemetry spans
            "span{service.name=\"api-gateway\",operation.name=\"route_request\",peer.service=\"user-service\",duration=125ms}",
            "span{service.name=\"order-service\",operation.name=\"create_order\",peer.service=\"payment-service\",duration=300ms}",
            "span{service.name=\"notification-service\",operation.name=\"send_email\",peer.service=\"email-gateway\",duration=150ms}",
            
            // APM dependency monitoring
            "dependency{source=\"user-service\",target=\"database\",type=\"sql\",response_time=45ms,success_rate=99.8}",
            "dependency{source=\"inventory-service\",target=\"redis-cache\",type=\"cache\",response_time=5ms,success_rate=99.9}",
            "dependency{source=\"api-gateway\",target=\"order-service\",type=\"http\",response_time=110ms,success_rate=99.2}",
            
            // Custom telemetry format
            "TELEMETRY 2024-07-04T10:30:45Z analytics-service -> kafka-broker throughput=1250.5 msg/sec",
            "TELEMETRY 2024-07-04T10:31:00Z monitoring-service -> prometheus latency=15ms",
            "TELEMETRY 2024-07-04T10:31:15Z mobile-app -> api-gateway requests=450 req/min"
        );
        
        return processFullPipelineWithAllSources(sampleRouterLogs, sampleCodebaseDeps, sampleApiLogs, sampleCiCdLogs, sampleTelemetryLogs);
    }
    
    /**
     * Complete end-to-end processing pipeline with all data sources including CI/CD and telemetry.
     * 
     * @param routerLogs Router log data as raw strings
     * @param codebaseDeps Codebase dependency data as raw strings
     * @param apiGatewayLogs API Gateway log data as raw strings
     * @param ciCdLogs CI/CD pipeline logs as raw strings
     * @param telemetryLogs Telemetry and monitoring data as raw strings
     * @return Complete dependency matrix with metrics
     */
    public DependencyMatrix processFullPipelineWithAllSources(
            List<String> routerLogs,
            List<String> codebaseDeps,
            List<String> apiGatewayLogs,
            List<String> ciCdLogs,
            List<String> telemetryLogs) {
        
        Instant startTime = Instant.now();
        logger.info("Starting full dependency matrix pipeline processing with all data sources");
        
        try {
            // Step 1: Data Ingestion - Convert raw data to claims from all sources
            List<Claim> allClaims = ingestAllData(routerLogs, codebaseDeps, apiGatewayLogs, ciCdLogs, telemetryLogs);
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
     * Step 1: Data Ingestion - Extract claims from all data sources including CI/CD and telemetry
     */
    private List<Claim> ingestAllData(List<String> routerLogs, List<String> codebaseDeps, 
                                     List<String> apiGatewayLogs, List<String> ciCdLogs, 
                                     List<String> telemetryLogs) {
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
            String combinedLogs = String.join("\n", apiGatewayLogs);
            List<Claim> apiClaims = apiGatewayAdapter.parseApiCalls(combinedLogs, "json");
            allClaims.addAll(apiClaims);
            logger.debug("Extracted {} claims from API gateway logs", apiClaims.size());
        }
        
        // Process CI/CD logs
        if (ciCdLogs != null && !ciCdLogs.isEmpty()) {
            List<Claim> ciCdClaims = ciCdAdapter.parseLogData(ciCdLogs);
            allClaims.addAll(ciCdClaims);
            logger.debug("Extracted {} claims from CI/CD logs", ciCdClaims.size());
        }
        
        // Process telemetry data
        if (telemetryLogs != null && !telemetryLogs.isEmpty()) {
            List<Claim> telemetryClaims = telemetryAdapter.parseLogData(telemetryLogs);
            allClaims.addAll(telemetryClaims);
            logger.debug("Extracted {} claims from telemetry data", telemetryClaims.size());
        }
        
        return allClaims;
    }

    /**
     * Alias method for processFullPipeline to maintain API compatibility
     */
    public DependencyMatrix processCompletePipeline(List<String> routerLogs, List<String> codebaseDeps, List<String> apiGatewayLogs) {
        return processFullPipeline(routerLogs, codebaseDeps, apiGatewayLogs);
    }
}

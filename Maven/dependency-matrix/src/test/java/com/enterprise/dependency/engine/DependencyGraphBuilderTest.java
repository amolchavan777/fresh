package com.enterprise.dependency.engine;

import com.enterprise.dependency.engine.DependencyGraphBuilder.DependencyGraph;
import com.enterprise.dependency.engine.DependencyGraphBuilder.GraphStats;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.model.core.Dependency;
import com.enterprise.dependency.model.core.DependencyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DependencyGraphBuilder.
 */
class DependencyGraphBuilderTest {

    private DependencyGraphBuilder graphBuilder;

    @BeforeEach
    void setUp() {
        graphBuilder = new DependencyGraphBuilder();
    }

    @Test
    void shouldBuildEmptyGraphForNullDependencies() {
        DependencyGraph graph = graphBuilder.buildGraph(null);
        
        assertNotNull(graph);
        assertTrue(graph.getNodes().isEmpty());
    }

    @Test
    void shouldBuildEmptyGraphForEmptyDependencies() {
        DependencyGraph graph = graphBuilder.buildGraph(Collections.emptyList());
        
        assertNotNull(graph);
        assertTrue(graph.getNodes().isEmpty());
    }

    @Test
    void shouldBuildSimpleGraph() {
        Dependency dep = Dependency.builder()
            .sourceAppId("app-a")
            .targetAppId("app-b")
            .type(DependencyType.API)
            .confidenceScore(ConfidenceScore.of(0.9))
            .build();

        DependencyGraph graph = graphBuilder.buildGraph(Arrays.asList(dep));

        assertEquals(2, graph.getNodes().size());
        assertTrue(graph.getNodes().contains("app-a"));
        assertTrue(graph.getNodes().contains("app-b"));
        
        Set<String> dependencies = graph.getDependencies("app-a");
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains("app-b"));
        
        Set<String> dependents = graph.getDependents("app-b");
        assertEquals(1, dependents.size());
        assertTrue(dependents.contains("app-a"));
    }

    @Test
    void shouldBuildComplexGraph() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("user-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("order-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.85))
                .build(),
            Dependency.builder()
                .sourceAppId("user-service")
                .targetAppId("user-database")
                .type(DependencyType.DATABASE)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build(),
            Dependency.builder()
                .sourceAppId("order-service")
                .targetAppId("order-database")
                .type(DependencyType.DATABASE)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build(),
            Dependency.builder()
                .sourceAppId("order-service")
                .targetAppId("user-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.8))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);

        assertEquals(5, graph.getNodes().size());
        
        // Check web-app dependencies
        Set<String> webAppDeps = graph.getDependencies("web-app");
        assertEquals(2, webAppDeps.size());
        assertTrue(webAppDeps.contains("user-service"));
        assertTrue(webAppDeps.contains("order-service"));
        
        // Check user-service dependents
        Set<String> userServiceDependents = graph.getDependents("user-service");
        assertEquals(2, userServiceDependents.size());
        assertTrue(userServiceDependents.contains("web-app"));
        assertTrue(userServiceDependents.contains("order-service"));
    }

    @Test
    void shouldDetectNoCyclesInAcyclicGraph() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-b")
                .targetAppId("app-c")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        List<List<String>> cycles = graph.detectCycles();

        assertTrue(cycles.isEmpty());
    }

    @Test
    void shouldDetectSimpleCycle() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-b")
                .targetAppId("app-c")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-c")
                .targetAppId("app-a")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        List<List<String>> cycles = graph.detectCycles();

        assertEquals(1, cycles.size());
        List<String> cycle = cycles.get(0);
        assertTrue(cycle.size() >= 3); // Should contain at least the 3 apps in the cycle
    }

    @Test
    void shouldPerformTopologicalSortOnAcyclicGraph() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("user-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("user-service")
                .targetAppId("user-database")
                .type(DependencyType.DATABASE)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build(),
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("order-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.85))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        List<String> topologicalOrder = graph.getTopologicalOrder();

        assertEquals(4, topologicalOrder.size());
        
        // Dependencies should appear before their targets in the order
        int webAppIndex = topologicalOrder.indexOf("web-app");
        int userServiceIndex = topologicalOrder.indexOf("user-service");
        int userDbIndex = topologicalOrder.indexOf("user-database");
        int orderServiceIndex = topologicalOrder.indexOf("order-service");
        
        assertTrue(webAppIndex < userServiceIndex);
        assertTrue(userServiceIndex < userDbIndex);
        assertTrue(webAppIndex < orderServiceIndex);
    }

    @Test
    void shouldThrowExceptionForTopologicalSortOnCyclicGraph() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-b")
                .targetAppId("app-a")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);

        assertThrows(IllegalStateException.class, () -> graph.getTopologicalOrder());
    }

    @Test
    void shouldFindShortestPath() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-b")
                .targetAppId("app-c")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-d")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-d")
                .targetAppId("app-c")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        List<String> path = graph.findShortestPath("app-a", "app-c");

        assertEquals(3, path.size());
        assertEquals("app-a", path.get(0));
        assertEquals("app-c", path.get(2));
        // Middle node should be either app-b or app-d
        assertTrue(path.get(1).equals("app-b") || path.get(1).equals("app-d"));
    }

    @Test
    void shouldReturnEmptyPathWhenNoPathExists() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            // app-c is isolated
            Dependency.builder()
                .sourceAppId("app-c")
                .targetAppId("app-d")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        List<String> path = graph.findShortestPath("app-a", "app-c");

        assertTrue(path.isEmpty());
    }

    @Test
    void shouldExtractSubgraph() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("user-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("web-app")
                .targetAppId("order-service")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.85))
                .build(),
            Dependency.builder()
                .sourceAppId("user-service")
                .targetAppId("user-database")
                .type(DependencyType.DATABASE)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build(),
            Dependency.builder()
                .sourceAppId("order-service")
                .targetAppId("order-database")
                .type(DependencyType.DATABASE)
                .confidenceScore(ConfidenceScore.of(0.95))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        
        // Extract subgraph containing only user-related components
        Set<String> userComponents = Set.of("web-app", "user-service", "user-database");
        DependencyGraph subgraph = graph.extractSubgraph(userComponents);

        assertEquals(3, subgraph.getNodes().size());
        assertTrue(subgraph.getNodes().contains("web-app"));
        assertTrue(subgraph.getNodes().contains("user-service"));
        assertTrue(subgraph.getNodes().contains("user-database"));
        assertFalse(subgraph.getNodes().contains("order-service"));
        assertFalse(subgraph.getNodes().contains("order-database"));
        
        // Check that dependencies are preserved within subgraph
        assertTrue(subgraph.getDependencies("web-app").contains("user-service"));
        assertTrue(subgraph.getDependencies("user-service").contains("user-database"));
        assertFalse(subgraph.getDependencies("web-app").contains("order-service"));
    }

    @Test
    void shouldCalculateGraphStatistics() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            Dependency.builder()
                .sourceAppId("app-b")
                .targetAppId("app-c")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build(),
            // Isolated component
            Dependency.builder()
                .sourceAppId("app-d")
                .targetAppId("app-e")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);
        GraphStats stats = graph.getStatistics();

        assertEquals(5, stats.getNodeCount());
        assertEquals(3, stats.getEdgeCount());
        assertEquals(0, stats.getCycleCount());
        assertEquals(2, stats.getComponentCount()); // Two separate components
    }

    @Test
    void shouldHandleIsolatedNodes() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-b")
                .type(DependencyType.API)
                .confidenceScore(ConfidenceScore.of(0.9))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);

        assertEquals(2, graph.getNodes().size());
        assertTrue(graph.getDependencies("app-b").isEmpty()); // app-b has no outgoing dependencies
        assertTrue(graph.getDependents("app-a").isEmpty()); // app-a has no incoming dependencies
    }

    @Test
    void shouldHandleSelfLoops() {
        List<Dependency> dependencies = Arrays.asList(
            Dependency.builder()
                .sourceAppId("app-a")
                .targetAppId("app-a") // Self dependency
                .type(DependencyType.RUNTIME)
                .confidenceScore(ConfidenceScore.of(0.8))
                .build()
        );

        DependencyGraph graph = graphBuilder.buildGraph(dependencies);

        assertEquals(1, graph.getNodes().size());
        assertTrue(graph.getDependencies("app-a").contains("app-a"));
        assertTrue(graph.getDependents("app-a").contains("app-a"));
        
        // Self loops create cycles
        List<List<String>> cycles = graph.detectCycles();
        assertEquals(1, cycles.size());
    }
}

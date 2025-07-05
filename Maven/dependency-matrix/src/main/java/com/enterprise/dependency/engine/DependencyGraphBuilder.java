package com.enterprise.dependency.engine;

import com.enterprise.dependency.model.core.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DependencyGraphBuilder constructs a graph representation of application dependencies
 * and provides graph analysis capabilities.
 * 
 * <p>Features include:
 * <ul>
 *   <li>Directed graph construction from dependency list</li>
 *   <li>Cycle detection for identifying circular dependencies</li>
 *   <li>Topological sorting for deployment ordering</li>
 *   <li>Critical path analysis for impact assessment</li>
 *   <li>Subgraph extraction for specific application analysis</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   DependencyGraphBuilder builder = new DependencyGraphBuilder();
 *   DependencyGraph graph = builder.buildGraph(dependencies);
 *   List&lt;String&gt; cycles = graph.detectCycles();
 *   List&lt;String&gt; deploymentOrder = graph.getTopologicalOrder();
 * </pre>
 */
@Service
public class DependencyGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

    /**
     * Builds a dependency graph from a list of dependencies.
     * 
     * @param dependencies List of dependency relationships
     * @return DependencyGraph representing the application ecosystem
     */
    public DependencyGraph buildGraph(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            logger.warn("No dependencies provided for graph construction");
            return new DependencyGraph(Collections.emptyMap());
        }
        
        logger.info("Building dependency graph from {} dependencies", dependencies.size());
        
        // Build adjacency list representation
        Map<String, Set<GraphEdge>> adjacencyList = new HashMap<>();
        Set<String> allNodes = new HashSet<>();
        
        for (Dependency dep : dependencies) {
            String source = dep.getSourceAppId();
            String target = dep.getTargetAppId();
            
            allNodes.add(source);
            allNodes.add(target);
            
            GraphEdge edge = new GraphEdge(target, dep.getType().toString(), 
                dep.getConfidenceScore().getValue());
            
            adjacencyList.computeIfAbsent(source, k -> new HashSet<>()).add(edge);
        }
        
        // Ensure all nodes are represented (even isolated ones)
        for (String node : allNodes) {
            adjacencyList.putIfAbsent(node, new HashSet<>());
        }
        
        DependencyGraph graph = new DependencyGraph(adjacencyList);
        
        logger.info("Built dependency graph with {} nodes and {} edges", 
            allNodes.size(), dependencies.size());
        
        return graph;
    }

    /**
     * Represents a dependency graph with analysis capabilities.
     */
    public static class DependencyGraph {
        private final Map<String, Set<GraphEdge>> adjacencyList;
        
        public DependencyGraph(Map<String, Set<GraphEdge>> adjacencyList) {
            this.adjacencyList = new HashMap<>(adjacencyList);
        }
        
        /**
         * Gets all nodes in the graph.
         */
        public Set<String> getNodes() {
            return Collections.unmodifiableSet(adjacencyList.keySet());
        }
        
        /**
         * Gets direct dependencies of a given application.
         */
        public Set<String> getDependencies(String application) {
            return adjacencyList.getOrDefault(application, Collections.emptySet())
                .stream()
                .map(GraphEdge::getTarget)
                .collect(Collectors.toSet());
        }
        
        /**
         * Gets all applications that depend on the given application.
         */
        public Set<String> getDependents(String application) {
            Set<String> dependents = new HashSet<>();
            
            for (Map.Entry<String, Set<GraphEdge>> entry : adjacencyList.entrySet()) {
                String source = entry.getKey();
                Set<GraphEdge> edges = entry.getValue();
                
                boolean dependsOnTarget = edges.stream()
                    .anyMatch(edge -> edge.getTarget().equals(application));
                
                if (dependsOnTarget) {
                    dependents.add(source);
                }
            }
            
            return dependents;
        }
        
        /**
         * Detects cycles in the dependency graph.
         * 
         * @return List of cycles, where each cycle is represented as a list of application names
         */
        public List<List<String>> detectCycles() {
            List<List<String>> cycles = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();
            
            for (String node : adjacencyList.keySet()) {
                if (!visited.contains(node)) {
                    List<String> currentPath = new ArrayList<>();
                    detectCyclesUtil(node, visited, recursionStack, currentPath, cycles);
                }
            }
            
            return cycles;
        }
        
        private void detectCyclesUtil(String node, Set<String> visited, Set<String> recursionStack,
                                    List<String> currentPath, List<List<String>> cycles) {
            visited.add(node);
            recursionStack.add(node);
            currentPath.add(node);
            
            Set<GraphEdge> neighbors = adjacencyList.getOrDefault(node, Collections.emptySet());
            
            for (GraphEdge edge : neighbors) {
                String neighbor = edge.getTarget();
                
                if (!visited.contains(neighbor)) {
                    detectCyclesUtil(neighbor, visited, recursionStack, currentPath, cycles);
                } else if (recursionStack.contains(neighbor)) {
                    // Found a cycle - extract the cycle from current path
                    int cycleStart = currentPath.indexOf(neighbor);
                    if (cycleStart >= 0) {
                        List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                        cycle.add(neighbor); // Complete the cycle
                        cycles.add(cycle);
                    }
                }
            }
            
            recursionStack.remove(node);
            currentPath.remove(currentPath.size() - 1);
        }
        
        /**
         * Performs topological sorting of the dependency graph.
         * 
         * @return List of applications in topological order (safe deployment order)
         * @throws IllegalStateException if the graph contains cycles
         */
        public List<String> getTopologicalOrder() {
            if (!detectCycles().isEmpty()) {
                throw new IllegalStateException("Cannot perform topological sort on graph with cycles");
            }
            
            Map<String, Integer> inDegree = calculateInDegrees();
            Queue<String> queue = new LinkedList<>();
            List<String> result = new ArrayList<>();
            
            // Start with nodes that have no incoming edges
            for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
                if (entry.getValue() == 0) {
                    queue.offer(entry.getKey());
                }
            }
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                result.add(current);
                
                // Reduce in-degree for all neighbors
                Set<GraphEdge> neighbors = adjacencyList.getOrDefault(current, Collections.emptySet());
                for (GraphEdge edge : neighbors) {
                    String neighbor = edge.getTarget();
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    
                    if (inDegree.get(neighbor) == 0) {
                        queue.offer(neighbor);
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Calculates in-degrees for all nodes in the graph.
         */
        private Map<String, Integer> calculateInDegrees() {
            Map<String, Integer> inDegree = new HashMap<>();
            
            // Initialize all nodes with in-degree 0
            for (String node : adjacencyList.keySet()) {
                inDegree.put(node, 0);
            }
            
            // Count incoming edges for each node
            for (Set<GraphEdge> edges : adjacencyList.values()) {
                for (GraphEdge edge : edges) {
                    String target = edge.getTarget();
                    inDegree.put(target, inDegree.getOrDefault(target, 0) + 1);
                }
            }
            
            return inDegree;
        }
        
        /**
         * Finds the shortest path between two applications.
         * 
         * @param source Source application
         * @param target Target application
         * @return List representing the shortest path, or empty list if no path exists
         */
        public List<String> findShortestPath(String source, String target) {
            if (!adjacencyList.containsKey(source) || !adjacencyList.containsKey(target)) {
                return Collections.emptyList();
            }
            
            if (source.equals(target)) {
                return Arrays.asList(source);
            }
            
            Queue<String> queue = new LinkedList<>();
            Map<String, String> parent = new HashMap<>();
            Set<String> visited = new HashSet<>();
            
            queue.offer(source);
            visited.add(source);
            parent.put(source, null);
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                
                Set<GraphEdge> neighbors = adjacencyList.getOrDefault(current, Collections.emptySet());
                for (GraphEdge edge : neighbors) {
                    String neighbor = edge.getTarget();
                    
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.offer(neighbor);
                        
                        if (neighbor.equals(target)) {
                            // Reconstruct path
                            List<String> path = new ArrayList<>();
                            String node = target;
                            while (node != null) {
                                path.add(0, node);
                                node = parent.get(node);
                            }
                            return path;
                        }
                    }
                }
            }
            
            return Collections.emptyList(); // No path found
        }
        
        /**
         * Extracts a subgraph containing only the specified applications and their relationships.
         * 
         * @param applicationIds Set of application IDs to include in the subgraph
         * @return New DependencyGraph containing only the specified applications
         */
        public DependencyGraph extractSubgraph(Set<String> applicationIds) {
            Map<String, Set<GraphEdge>> subgraphAdjacencyList = new HashMap<>();
            
            for (String app : applicationIds) {
                if (adjacencyList.containsKey(app)) {
                    Set<GraphEdge> filteredEdges = adjacencyList.get(app).stream()
                        .filter(edge -> applicationIds.contains(edge.getTarget()))
                        .collect(Collectors.toSet());
                    subgraphAdjacencyList.put(app, filteredEdges);
                }
            }
            
            return new DependencyGraph(subgraphAdjacencyList);
        }
        
        /**
         * Gets graph statistics.
         */
        public GraphStats getStatistics() {
            int nodeCount = adjacencyList.size();
            int edgeCount = adjacencyList.values().stream()
                .mapToInt(Set::size)
                .sum();
            
            int cycleCount = detectCycles().size();
            
            // Calculate connected components
            int componentCount = countConnectedComponents();
            
            return new GraphStats(nodeCount, edgeCount, cycleCount, componentCount);
        }
        
        private int countConnectedComponents() {
            Set<String> visited = new HashSet<>();
            int componentCount = 0;
            
            for (String node : adjacencyList.keySet()) {
                if (!visited.contains(node)) {
                    dfsVisit(node, visited);
                    componentCount++;
                }
            }
            
            return componentCount;
        }
        
        private void dfsVisit(String node, Set<String> visited) {
            visited.add(node);
            
            // Visit outgoing edges
            Set<GraphEdge> outgoing = adjacencyList.getOrDefault(node, Collections.emptySet());
            for (GraphEdge edge : outgoing) {
                if (!visited.contains(edge.getTarget())) {
                    dfsVisit(edge.getTarget(), visited);
                }
            }
            
            // Visit incoming edges (treat graph as undirected for component analysis)
            for (Map.Entry<String, Set<GraphEdge>> entry : adjacencyList.entrySet()) {
                String source = entry.getKey();
                if (!visited.contains(source)) {
                    boolean hasEdgeToNode = entry.getValue().stream()
                        .anyMatch(edge -> edge.getTarget().equals(node));
                    if (hasEdgeToNode) {
                        dfsVisit(source, visited);
                    }
                }
            }
        }
    }
    
    /**
     * Represents an edge in the dependency graph.
     */
    public static class GraphEdge {
        private final String target;
        private final String dependencyType;
        private final double confidence;
        
        public GraphEdge(String target, String dependencyType, double confidence) {
            this.target = target;
            this.dependencyType = dependencyType;
            this.confidence = confidence;
        }
        
        public String getTarget() { return target; }
        public String getDependencyType() { return dependencyType; }
        public double getConfidence() { return confidence; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GraphEdge graphEdge = (GraphEdge) o;
            return Objects.equals(target, graphEdge.target) &&
                   Objects.equals(dependencyType, graphEdge.dependencyType);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(target, dependencyType);
        }
        
        @Override
        public String toString() {
            return String.format("-> %s [%s] (%.2f)", target, dependencyType, confidence);
        }
    }
    
    /**
     * Graph statistics.
     */
    public static class GraphStats {
        private final int nodeCount;
        private final int edgeCount;
        private final int cycleCount;
        private final int componentCount;
        
        public GraphStats(int nodeCount, int edgeCount, int cycleCount, int componentCount) {
            this.nodeCount = nodeCount;
            this.edgeCount = edgeCount;
            this.cycleCount = cycleCount;
            this.componentCount = componentCount;
        }
        
        public int getNodeCount() { return nodeCount; }
        public int getEdgeCount() { return edgeCount; }
        public int getCycleCount() { return cycleCount; }
        public int getComponentCount() { return componentCount; }
        
        @Override
        public String toString() {
            return String.format("GraphStats{nodes=%d, edges=%d, cycles=%d, components=%d}", 
                nodeCount, edgeCount, cycleCount, componentCount);
        }
    }
}

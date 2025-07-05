package com.enterprise.dependency.model.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DependencyMatrix represents the final output of the dependency analysis pipeline.
 * Contains the complete dependency graph, metadata, and processing metrics.
 * 
 * This is the main result object that contains all discovered dependencies
 * and can be used for visualization, reporting, and further analysis.
 */
public class DependencyMatrix {
    private final List<Dependency> dependencies;
    private final Map<String, Object> graph;
    private final int applicationCount;
    private final int dependencyCount;
    private final long processingTimeMs;
    private final Instant timestamp;
    private final String version;
    
    private DependencyMatrix(Builder builder) {
        this.dependencies = builder.dependencies;
        this.graph = builder.graph;
        this.applicationCount = builder.applicationCount;
        this.dependencyCount = builder.dependencyCount;
        this.processingTimeMs = builder.processingTimeMs;
        this.timestamp = builder.timestamp;
        this.version = builder.version != null ? builder.version : "1.0";
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public List<Dependency> getDependencies() { return dependencies; }
    public Map<String, Object> getGraph() { return graph; }
    public int getApplicationCount() { return applicationCount; }
    public int getDependencyCount() { return dependencyCount; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public Instant getTimestamp() { return timestamp; }
    public String getVersion() { return version; }
    
    /**
     * Get processing time in human-readable format
     */
    public String getProcessingTimeFormatted() {
        if (processingTimeMs < 1000) {
            return processingTimeMs + "ms";
        } else if (processingTimeMs < 60000) {
            return String.format("%.1fs", processingTimeMs / 1000.0);
        } else {
            long minutes = processingTimeMs / 60000;
            long seconds = (processingTimeMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Get matrix summary statistics
     */
    public MatrixSummary getSummary() {
        return MatrixSummary.builder()
            .totalApplications(applicationCount)
            .totalDependencies(dependencyCount)
            .processingTime(getProcessingTimeFormatted())
            .timestamp(timestamp)
            .averageDependenciesPerApp(applicationCount > 0 ? (double) dependencyCount / applicationCount : 0.0)
            .build();
    }
    
    @Override
    public String toString() {
        return String.format("DependencyMatrix{apps=%d, deps=%d, time=%s}", 
            applicationCount, dependencyCount, getProcessingTimeFormatted());
    }
    
    public static class Builder {
        private List<Dependency> dependencies;
        private Map<String, Object> graph;
        private int applicationCount;
        private int dependencyCount;
        private long processingTimeMs;
        private Instant timestamp;
        private String version;
        
        public Builder dependencies(List<Dependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }
        
        public Builder graph(Map<String, Object> graph) {
            this.graph = graph;
            return this;
        }
        
        public Builder applicationCount(int applicationCount) {
            this.applicationCount = applicationCount;
            return this;
        }
        
        public Builder dependencyCount(int dependencyCount) {
            this.dependencyCount = dependencyCount;
            return this;
        }
        
        public Builder processingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public DependencyMatrix build() {
            return new DependencyMatrix(this);
        }
    }
    
    /**
     * Summary statistics for the dependency matrix
     */
    public static class MatrixSummary {
        private final int totalApplications;
        private final int totalDependencies;
        private final String processingTime;
        private final Instant timestamp;
        private final double averageDependenciesPerApp;
        
        private MatrixSummary(Builder builder) {
            this.totalApplications = builder.totalApplications;
            this.totalDependencies = builder.totalDependencies;
            this.processingTime = builder.processingTime;
            this.timestamp = builder.timestamp;
            this.averageDependenciesPerApp = builder.averageDependenciesPerApp;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public int getTotalApplications() { return totalApplications; }
        public int getTotalDependencies() { return totalDependencies; }
        public String getProcessingTime() { return processingTime; }
        public Instant getTimestamp() { return timestamp; }
        public double getAverageDependenciesPerApp() { return averageDependenciesPerApp; }
        
        public static class Builder {
            private int totalApplications;
            private int totalDependencies;
            private String processingTime;
            private Instant timestamp;
            private double averageDependenciesPerApp;
            
            public Builder totalApplications(int totalApplications) {
                this.totalApplications = totalApplications;
                return this;
            }
            
            public Builder totalDependencies(int totalDependencies) {
                this.totalDependencies = totalDependencies;
                return this;
            }
            
            public Builder processingTime(String processingTime) {
                this.processingTime = processingTime;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public Builder averageDependenciesPerApp(double averageDependenciesPerApp) {
                this.averageDependenciesPerApp = averageDependenciesPerApp;
                return this;
            }
            
            public MatrixSummary build() {
                return new MatrixSummary(this);
            }
        }
    }
}

package com.enterprise.dependency.model.sources;

import java.time.Instant;

/**
 * Represents a CI/CD pipeline event that indicates deployment or build dependencies.
 * 
 * <p>CI/CD events show how different stages, services, or components depend on each other
 * in deployment pipelines, build processes, and release workflows.
 */
public class CiCdEvent {
    private Instant timestamp;
    private String sourceStage;
    private String targetStage;
    private String action;
    private String status;
    private String platform;
    private String rawLine;
    private String jobId;
    private String pipelineId;
    
    // Private constructor for builder
    private CiCdEvent() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public Instant getTimestamp() { return timestamp; }
    public String getSourceStage() { return sourceStage; }
    public String getTargetStage() { return targetStage; }
    public String getAction() { return action; }
    public String getStatus() { return status; }
    public String getPlatform() { return platform; }
    public String getRawLine() { return rawLine; }
    public String getJobId() { return jobId; }
    public String getPipelineId() { return pipelineId; }
    
    @Override
    public String toString() {
        return String.format("CiCdEvent{timestamp=%s, sourceStage='%s', targetStage='%s', action='%s', status='%s', platform='%s'}",
            timestamp, sourceStage, targetStage, action, status, platform);
    }
    
    /**
     * Builder class for CiCdEvent
     */
    public static class Builder {
        private final CiCdEvent event = new CiCdEvent();
        
        public Builder timestamp(Instant timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public Builder sourceStage(String sourceStage) {
            event.sourceStage = sourceStage;
            return this;
        }
        
        public Builder targetStage(String targetStage) {
            event.targetStage = targetStage;
            return this;
        }
        
        public Builder action(String action) {
            event.action = action;
            return this;
        }
        
        public Builder status(String status) {
            event.status = status;
            return this;
        }
        
        public Builder platform(String platform) {
            event.platform = platform;
            return this;
        }
        
        public Builder rawLine(String rawLine) {
            event.rawLine = rawLine;
            return this;
        }
        
        public Builder jobId(String jobId) {
            event.jobId = jobId;
            return this;
        }
        
        public Builder pipelineId(String pipelineId) {
            event.pipelineId = pipelineId;
            return this;
        }
        
        public CiCdEvent build() {
            // Validation
            if (event.sourceStage == null || event.targetStage == null) {
                throw new IllegalArgumentException("sourceStage and targetStage are required");
            }
            if (event.timestamp == null) {
                event.timestamp = Instant.now();
            }
            return event;
        }
    }
}

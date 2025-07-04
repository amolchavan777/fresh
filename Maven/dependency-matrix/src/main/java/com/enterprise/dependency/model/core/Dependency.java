package com.enterprise.dependency.model.core;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a dependency relationship between two applications.
 * <p>
 * Example:
 * <pre>
 *   Dependency dep = Dependency.builder()
 *       .sourceAppId("app-001")
 *       .targetAppId("app-002")
 *       .type(DependencyType.RUNTIME)
 *       .confidenceScore(ConfidenceScore.of(0.95))
 *       .build();
 * </pre>
 */
public class Dependency {
    private static final Logger logger = LoggerFactory.getLogger(Dependency.class);

    @NotNull
    private final String sourceAppId;
    @NotNull
    private final String targetAppId;
    @NotNull
    private final DependencyType type;
    @NotNull
    private final ConfidenceScore confidenceScore;

    private Dependency(Builder builder) {
        this.sourceAppId = builder.sourceAppId;
        this.targetAppId = builder.targetAppId;
        this.type = builder.type;
        this.confidenceScore = builder.confidenceScore;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (sourceAppId == null || sourceAppId.isEmpty()) throw new IllegalArgumentException("sourceAppId is required");
        if (targetAppId == null || targetAppId.isEmpty()) throw new IllegalArgumentException("targetAppId is required");
        if (type == null) throw new IllegalArgumentException("type is required");
        if (confidenceScore == null) throw new IllegalArgumentException("confidenceScore is required");
        logger.debug("Validated Dependency: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public String getSourceAppId() { return sourceAppId; }
    public String getTargetAppId() { return targetAppId; }
    public DependencyType getType() { return type; }
    public ConfidenceScore getConfidenceScore() { return confidenceScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(sourceAppId, that.sourceAppId) &&
               Objects.equals(targetAppId, that.targetAppId) &&
               type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAppId, targetAppId, type);
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "sourceAppId='" + sourceAppId + '\'' +
                ", targetAppId='" + targetAppId + '\'' +
                ", type=" + type +
                ", confidenceScore=" + confidenceScore +
                '}';
    }

    /**
     * Builder for Dependency.
     */
    public static class Builder {
        private String sourceAppId;
        private String targetAppId;
        private DependencyType type;
        private ConfidenceScore confidenceScore;

        public Builder sourceAppId(String sourceAppId) { this.sourceAppId = sourceAppId; return this; }
        public Builder targetAppId(String targetAppId) { this.targetAppId = targetAppId; return this; }
        public Builder type(DependencyType type) { this.type = type; return this; }
        public Builder confidenceScore(ConfidenceScore confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Dependency build() { return new Dependency(this); }
    }
}

package com.enterprise.dependency.model.sources;

import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a dependency as declared in a codebase (e.g., Maven/Gradle/NPM).
 * <p>
 * Example:
 * <pre>
 *   CodebaseDependency dep = CodebaseDependency.builder()
 *       .id("unique-id")
 *       .groupId("com.enterprise")
 *       .artifactId("user-service-client")
 *       .version("2.1.0")
 *       .dependencyType("maven")
 *       .sourceApplication("order-service")
 *       .targetApplication("user-service")
 *       .rawXml("<dependency>...</dependency>")
 *       .timestamp(Instant.now())
 *       .build();
 * </pre>
 */
public class CodebaseDependency {
    private static final Logger logger = LoggerFactory.getLogger(CodebaseDependency.class);

    @NotBlank
    private final String id;
    @NotBlank
    private final String groupId;
    @NotBlank
    private final String artifactId;
    @NotBlank
    private final String version;
    @NotBlank
    private final String dependencyType;
    @NotBlank
    private final String sourceApplication;
    @NotBlank
    private final String targetApplication;
    @NotBlank
    private final String rawXml;
    private final Instant timestamp;

    private CodebaseDependency(Builder builder) {
        this.id = builder.id;
        this.groupId = builder.groupId;
        this.artifactId = builder.artifactId;
        this.version = builder.version;
        this.dependencyType = builder.dependencyType;
        this.sourceApplication = builder.sourceApplication;
        this.targetApplication = builder.targetApplication;
        this.rawXml = builder.rawXml;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        validate();
    }

    private void validate() {
        validateRequired();
        logger.debug("Validated CodebaseDependency: {}", this);
    }
    
    private void validateRequired() {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id is required");
        if (groupId == null || groupId.isEmpty()) throw new IllegalArgumentException("groupId is required");
        if (artifactId == null || artifactId.isEmpty()) throw new IllegalArgumentException("artifactId is required");
        if (version == null || version.isEmpty()) throw new IllegalArgumentException("version is required");
        validateApplicationAndType();
    }
    
    private void validateApplicationAndType() {
        if (dependencyType == null || dependencyType.isEmpty()) throw new IllegalArgumentException("dependencyType is required");
        if (sourceApplication == null || sourceApplication.isEmpty()) throw new IllegalArgumentException("sourceApplication is required");
        if (targetApplication == null || targetApplication.isEmpty()) throw new IllegalArgumentException("targetApplication is required");
        if (rawXml == null || rawXml.isEmpty()) throw new IllegalArgumentException("rawXml is required");
    }

    public static Builder builder() { return new Builder(); }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public String getDependencyType() { return dependencyType; }
    public String getSourceApplication() { return sourceApplication; }
    public String getTargetApplication() { return targetApplication; }
    public String getRawXml() { return rawXml; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodebaseDependency that = (CodebaseDependency) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version) &&
               Objects.equals(dependencyType, that.dependencyType) &&
               Objects.equals(sourceApplication, that.sourceApplication) &&
               Objects.equals(targetApplication, that.targetApplication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, artifactId, version, dependencyType, 
                          sourceApplication, targetApplication);
    }

    @Override
    public String toString() {
        return "CodebaseDependency{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", dependencyType='" + dependencyType + '\'' +
                ", sourceApplication='" + sourceApplication + '\'' +
                ", targetApplication='" + targetApplication + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Builder for CodebaseDependency.
     */
    public static class Builder {
        private String id;
        private String groupId;
        private String artifactId;
        private String version;
        private String dependencyType;
        private String sourceApplication;
        private String targetApplication;
        private String rawXml;
        private Instant timestamp;

        public Builder id(String id) { this.id = id; return this; }
        public Builder groupId(String groupId) { this.groupId = groupId; return this; }
        public Builder artifactId(String artifactId) { this.artifactId = artifactId; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder dependencyType(String dependencyType) { this.dependencyType = dependencyType; return this; }
        public Builder sourceApplication(String sourceApplication) { this.sourceApplication = sourceApplication; return this; }
        public Builder targetApplication(String targetApplication) { this.targetApplication = targetApplication; return this; }
        public Builder rawXml(String rawXml) { this.rawXml = rawXml; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public CodebaseDependency build() { return new CodebaseDependency(this); }
    }
}

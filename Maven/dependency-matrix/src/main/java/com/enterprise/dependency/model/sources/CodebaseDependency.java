package com.enterprise.dependency.model.sources;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a dependency as declared in a codebase (e.g., Maven/Gradle).
 * <p>
 * Example:
 * <pre>
 *   CodebaseDependency dep = CodebaseDependency.builder()
 *       .groupId("com.enterprise")
 *       .artifactId("user-service-client")
 *       .version("2.1.0")
 *       .rawXml("<dependency>...</dependency>")
 *       .build();
 * </pre>
 */
public class CodebaseDependency {
    private static final Logger logger = LoggerFactory.getLogger(CodebaseDependency.class);

    @NotBlank
    private final String groupId;
    @NotBlank
    private final String artifactId;
    @NotBlank
    private final String version;
    @NotBlank
    private final String rawXml;

    private CodebaseDependency(Builder builder) {
        this.groupId = builder.groupId;
        this.artifactId = builder.artifactId;
        this.version = builder.version;
        this.rawXml = builder.rawXml;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (groupId == null || groupId.isEmpty()) throw new IllegalArgumentException("groupId is required");
        if (artifactId == null || artifactId.isEmpty()) throw new IllegalArgumentException("artifactId is required");
        if (version == null || version.isEmpty()) throw new IllegalArgumentException("version is required");
        if (rawXml == null || rawXml.isEmpty()) throw new IllegalArgumentException("rawXml is required");
        logger.debug("Validated CodebaseDependency: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public String getGroupId() { return groupId; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public String getRawXml() { return rawXml; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodebaseDependency that = (CodebaseDependency) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return "CodebaseDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", rawXml='" + rawXml + '\'' +
                '}';
    }

    /**
     * Builder for CodebaseDependency.
     */
    public static class Builder {
        private String groupId;
        private String artifactId;
        private String version;
        private String rawXml;

        public Builder groupId(String groupId) { this.groupId = groupId; return this; }
        public Builder artifactId(String artifactId) { this.artifactId = artifactId; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder rawXml(String rawXml) { this.rawXml = rawXml; return this; }
        public CodebaseDependency build() { return new CodebaseDependency(this); }
    }
}

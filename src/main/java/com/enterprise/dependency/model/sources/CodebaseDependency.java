package com.enterprise.dependency.model.sources;

/**
 * Represents a codebase dependency as parsed from build files (e.g., Maven, Gradle).
 * <p>
 * Example usage:
 * <pre>
 *   CodebaseDependency dep = new CodebaseDependency();
 *   dep.setGroupId("com.enterprise");
 *   dep.setArtifactId("user-service-client");
 *   dep.setVersion("2.1.0");
 * </pre>
 */
public class CodebaseDependency {
    private String groupId;
    private String artifactId;
    private String version;

    // Getters and setters omitted for brevity
    // TODO: Add validation logic for groupId, artifactId, and version
}

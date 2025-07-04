package com.enterprise.dependency.model;

/**
 * Enum representing the source type of a dependency claim.
 * <p>
 * Example usage:
 * <pre>
 *   SourceType type = SourceType.LOG;
 * </pre>
 */
public enum SourceType {
    LOG,
    TELEMETRY,
    CODEBASE,
    CONFIG
}

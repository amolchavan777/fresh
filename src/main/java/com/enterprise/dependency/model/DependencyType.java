package com.enterprise.dependency.model;

/**
 * Enum representing the type of dependency between applications.
 * <p>
 * Example usage:
 * <pre>
 *   DependencyType type = DependencyType.RUNTIME;
 * </pre>
 */
public enum DependencyType {
    RUNTIME,
    BUILD_TIME
}

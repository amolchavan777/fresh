package com.enterprise.dependency.model.core;

/**
 * Enum representing types of dependencies between applications.
 *
 * <p>Examples: RUNTIME, BUILD, API, DATABASE, etc.</p>
 */
public enum DependencyType {
    RUNTIME,
    BUILD,
    API,
    DATABASE,
    NETWORK,
    OTHER
}

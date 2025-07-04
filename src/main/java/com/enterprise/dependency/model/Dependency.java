package com.enterprise.dependency.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.relational.core.mapping.Table;
import javax.persistence.*;
import java.util.UUID;

/**
 * Represents a dependency relationship between two applications.
 * <p>
 * Example usage:
 * <pre>
 *   Dependency dep = new Dependency();
 *   dep.setSourceAppId(UUID.randomUUID());
 *   dep.setTargetAppId(UUID.randomUUID());
 *   dep.setType(DependencyType.RUNTIME);
 *   dep.setConfidence(0.9f);
 * </pre>
 */
@Entity
@Table(name = "dependencies")
@Node("Dependency")
public class Dependency {
    @Id
    @org.springframework.data.neo4j.core.schema.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID sourceAppId;

    @Column(nullable = false)
    private UUID targetAppId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DependencyType type;

    @Column(nullable = false)
    private float confidence;

    // Getters and setters omitted for brevity
    // TODO: Add validation logic for confidence (0-1) and non-null fields
}

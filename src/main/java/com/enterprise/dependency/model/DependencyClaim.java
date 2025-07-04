package com.enterprise.dependency.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a claim about a dependency between two applications, as discovered from a data source.
 * <p>
 * Example usage:
 * <pre>
 *   DependencyClaim claim = new DependencyClaim();
 *   claim.setSourceApplication("appA");
 *   claim.setTargetApplication("appB");
 *   claim.setDependencyType(DependencyType.RUNTIME);
 *   claim.setSourceType(SourceType.LOG);
 *   claim.setConfidenceScore(0.9f);
 * </pre>
 */
@Entity
@Table(name = "claims")
@Node("DependencyClaim")
@EntityListeners(AuditingEntityListener.class)
public class DependencyClaim {
    @Id
    @org.springframework.data.neo4j.core.schema.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String sourceApplication;

    @Column(nullable = false)
    private String targetApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DependencyType dependencyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Lob
    @Column
    private String sourceDetails;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private float confidenceScore;

    @Column(nullable = false)
    private String sourceIdentifier;

    // Getters and setters omitted for brevity

    // TODO: Add validation logic for confidenceScore (0-1) and non-null fields
    // TODO: Add logging in setters for auditability
}

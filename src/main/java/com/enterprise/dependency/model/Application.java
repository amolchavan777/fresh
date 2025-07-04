package com.enterprise.dependency.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.relational.core.mapping.Table;
import javax.persistence.*;
import java.util.UUID;

/**
 * Represents an application in the dependency matrix.
 * <p>
 * Example usage:
 * <pre>
 *   Application app = new Application();
 *   app.setName("user-service");
 *   app.setType("service");
 * </pre>
 */
@Entity
@Table(name = "applications")
@Node("Application")
public class Application {
    @Id
    @org.springframework.data.neo4j.core.schema.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String owner;

    // Getters and setters omitted for brevity
    // TODO: Add validation logic for non-null fields
}

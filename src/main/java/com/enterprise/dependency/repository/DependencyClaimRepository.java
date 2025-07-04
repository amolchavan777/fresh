package com.enterprise.dependency.repository;

import com.enterprise.dependency.model.DependencyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository for DependencyClaim supporting both JPA and Neo4j.
 */
@Repository
public interface DependencyClaimRepository extends JpaRepository<DependencyClaim, UUID>, Neo4jRepository<DependencyClaim, UUID> {
    // TODO: Add custom query methods as needed
}

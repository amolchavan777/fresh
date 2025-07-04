package com.enterprise.dependency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Application Dependency Matrix system.
 * 
 * <p>This Spring Boot application serves as the core orchestrator for analyzing and mapping
 * dependencies between enterprise applications. The system performs the following key functions:</p>
 * 
 * <ul>
 *   <li><strong>Multi-Source Data Ingestion:</strong> Parses router logs, network logs, and codebase files</li>
 *   <li><strong>Intelligent Scoring:</strong> Applies configurable scoring rules to determine dependency confidence</li>
 *   <li><strong>Claim Processing:</strong> Normalizes, validates, and enriches raw dependency claims</li>
 *   <li><strong>RESTful API:</strong> Provides endpoints for parsing logs and managing scoring rules</li>
 * </ul>
 * 
 * <p><strong>Architecture Overview:</strong></p>
 * <pre>
 * ┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
 * │   Data Sources  │───▶│  Claim Engine    │───▶│   Dependency    │
 * │ • Router Logs   │    │ • Normalization  │    │     Matrix      │
 * │ • Network Logs  │    │ • Validation     │    │ • Applications  │
 * │ • Codebase      │    │ • Scoring        │    │ • Dependencies  │
 * └─────────────────┘    └──────────────────┘    └─────────────────┘
 * </pre>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Multi-database support (PostgreSQL for RDBMS, Neo4j for graph relationships)</li>
 *   <li>Configurable scoring algorithms with dynamic rule management</li>
 *   <li>Comprehensive validation and error handling</li>
 *   <li>RESTful endpoints for system interaction</li>
 *   <li>Extensive logging and monitoring capabilities</li>
 * </ul>
 * 
 * <p><strong>Database Configuration:</strong></p>
 * <p>Note: Database auto-configurations are excluded to allow manual configuration
 * in application.yml. This provides flexibility for different deployment environments.</p>
 * 
 * <pre>
 * Usage Examples:
 *   # Development mode
 *   $ mvn spring-boot:run
 *   
 *   # Production deployment
 *   $ java -jar dependency-matrix-1.0-SNAPSHOT.jar
 *   
 *   # With custom profile
 *   $ java -jar dependency-matrix-1.0-SNAPSHOT.jar --spring.profiles.active=prod
 * </pre>
 * 
 * @author Enterprise Architecture Team
 * @version 1.0-SNAPSHOT
 * @since 2025-07-05
 * @see com.enterprise.dependency.engine.ClaimProcessingEngine
 * @see com.enterprise.dependency.service.LogAndCodebaseParserService
 * @see com.enterprise.dependency.web.ParserController
 */
@SpringBootApplication(
    exclude = {
        // Exclude auto-configuration to allow manual database setup
        // This provides flexibility for different deployment scenarios
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class
    }
)
// @EnableJpaRepositories // Intentionally commented out to avoid automatic JPA initialization
                          // This allows for manual configuration based on environment requirements
@ComponentScan(basePackages = "com.enterprise.dependency") // Scan all components in the enterprise package
public class DependencyMatrixApplication {
    
    /**
     * Application entry point that initializes the Spring Boot context.
     * 
     * <p>This method bootstraps the entire dependency matrix system, including:</p>
     * <ul>
     *   <li>Spring component initialization and dependency injection</li>
     *   <li>Web server startup (default port 8080)</li>
     *   <li>Configuration property loading from application.yml</li>
     *   <li>Service layer initialization for claim processing</li>
     * </ul>
     * 
     * @param args Command line arguments passed to the application.
     *             Supports standard Spring Boot arguments like --spring.profiles.active=prod
     */
    public static void main(String[] args) {
        SpringApplication.run(DependencyMatrixApplication.class, args);
    }
}

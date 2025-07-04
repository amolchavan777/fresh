package com.enterprise.dependency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Application Dependency Matrix prototype.
 * <p>
 * Example usage:
 * <pre>
 *   mvn spring-boot:run
 * </pre>
 */
@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(basePackages = "com.enterprise.dependency")
public class DependencyMatrixApplication {
    public static void main(String[] args) {
        SpringApplication.run(DependencyMatrixApplication.class, args);
    }
}

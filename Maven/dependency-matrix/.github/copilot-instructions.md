# Copilot Instructions for Application Dependency Matrix

## Project Structure
- Java 11+ Spring Boot (Maven)
- Base package: `com.enterprise.dependency`
- Modules: model, repository, service, web
- Databases: PostgreSQL (RDBMS), Neo4j (Graph)

## Key Dependencies
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Spring Boot Starter Data Neo4j
- Spring Boot Starter Validation
- Lombok
- JUnit 5, Mockito

## Setup
- Configure `application.yml` for DB credentials
- Use `mvn clean install` to build
- Use `mvn spring-boot:run` to start

## Contribution
- Follow standard Java/Spring conventions
- Write unit tests for all business logic
- Document public APIs

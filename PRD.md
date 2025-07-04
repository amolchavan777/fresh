# GitHub Copilot Agent Prompt - Application Dependency Matrix System

## 🚀 MASTER PROMPT FOR GITHUB COPILOT AGENT

```
You are an expert Java Solution Architect tasked with developing an Enterprise Application Dependency Matrix system. This is a sophisticated data fusion project that discovers and tracks dependencies between ~150 applications using heterogeneous data sources.

## PROJECT CONTEXT & ARCHITECTURE

### Core Mission
Build a system that ingests data from 6 heterogeneous sources (router logs, codebase, CI/CD pipelines, API gateways, telemetry, network logs), applies data fusion techniques, resolves conflicts, and generates a comprehensive dependency matrix.

### Architecture Pattern
- **Pattern**: Layered architecture with data processing pipeline
- **Tech Stack**: Java 11+, Spring Boot, H2/PostgreSQL, Maven
- **Deployment**: On-premises with hybrid cloud support
- **Scale**: 150 applications, 10,000+ dependency relationships

### Key Components to Build
1. **Data Source Adapters** (6 different types)
2. **Claim Processing Engine** (normalization + confidence scoring)
3. **Inference Engine** (rule-based dependency inference)
4. **Conflict Resolution Engine** (weighted voting algorithms)
5. **Dependency Graph Builder** (with cycle detection)
6. **Web Dashboard** (Spring Boot + Thymeleaf/React)
7. **Integration Layer** (CMDB, ITSM, monitoring tools)

## 🎯 DEVELOPMENT PRIORITIES

### Phase 1: Foundation (Start Here)
**IMMEDIATE FOCUS**: Create the core metamodel and basic data ingestion framework

**Key Classes to Generate First:**
```java
// Core Domain Models
com.enterprise.dependency.model.core.Application
com.enterprise.dependency.model.core.Dependency
com.enterprise.dependency.model.core.DependencyType
com.enterprise.dependency.model.core.Claim
com.enterprise.dependency.model.core.ConfidenceScore

// Data Source Models
com.enterprise.dependency.model.sources.RouterLogEntry
com.enterprise.dependency.model.sources.CodebaseDependency
com.enterprise.dependency.model.sources.ApiGatewayCall
```

### Critical Requirements
- **Accuracy Target**: 95% dependency detection accuracy
- **Performance**: Process full analysis within 4 hours
- **Conflict Resolution**: Weighted voting with configurable source priorities
- **Data Quality**: Robust validation and cleansing
- **Extensibility**: Easy to add new data sources

## 💡 CODING GUIDELINES & BEST PRACTICES

### Code Quality Standards
- Use **Builder Pattern** for complex objects
- Implement **Strategy Pattern** for different data source adapters
- Apply **Command Pattern** for processing pipeline steps
- Use **Factory Pattern** for creating adapters
- Implement **Observer Pattern** for processing status updates

### Naming Conventions
- Classes: PascalCase (e.g., `RouterLogAdapter`)
- Methods: camelCase (e.g., `processClaims()`)
- Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_CONFIDENCE_THRESHOLD`)
- Packages: lowercase with dots (e.g., `com.enterprise.dependency.engine`)

### Error Handling
- Use custom exceptions for domain-specific errors
- Implement comprehensive logging with SLF4J
- Add retry mechanisms for external system calls
- Include circuit breaker patterns for resilience

## 🔧 TECHNICAL SPECIFICATIONS

### Data Processing Pipeline
```
Raw Data → Normalize → Validate → Create Claims → Apply Inference → 
Resolve Conflicts → Build Graph → Analyze → Export
```

### Confidence Scoring Algorithm
- **Router Logs**: 0.9 (high confidence - direct communication)
- **Codebase**: 0.95 (highest confidence - explicit dependencies)
- **CI/CD**: 0.8 (medium-high - deployment dependencies)
- **API Gateway**: 0.85 (high - runtime dependencies)
- **Telemetry**: 0.7 (medium - observed behavior)
- **Network**: 0.6 (lower - may include noise)

### Conflict Resolution Rules
1. **Source Priority**: Codebase > Router Logs > API Gateway > CI/CD > Telemetry > Network
2. **Recency Weight**: More recent data gets higher weight
3. **Frequency Weight**: Frequently observed dependencies get higher confidence
4. **Business Rules**: Manual overrides for known exceptions

## 📊 SAMPLE DATA SCENARIOS TO HANDLE

### Router Log Sample
```
2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 GET /api/users 200 125ms
2024-07-04 10:30:46 [INFO] 192.168.1.200 -> 192.168.1.150:3306 TCP connection established
```

### Codebase Dependency Sample
```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>user-service-client</artifactId>
    <version>2.1.0</version>
</dependency>
```

### API Gateway Sample
```json
{
  "timestamp": "2024-07-04T10:30:45Z",
  "source_service": "web-portal",
  "target_service": "user-service",
  "endpoint": "/api/users",
  "method": "GET",
  "response_time": 125
}
```

## 🎨 IMPLEMENTATION APPROACH

### Start with This Exact Structure
```java
@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(basePackages = "com.enterprise.dependency")
public class DependencyMatrixApplication {
    public static void main(String[] args) {
        SpringApplication.run(DependencyMatrixApplication.class, args);
    }
}
```

### Key Annotations to Use
- `@Service` for business logic classes
- `@Repository` for data access classes
- `@Component` for utility classes
- `@Configuration` for Spring configuration
- `@Value` for externalized configuration
- `@Transactional` for database operations
- `@Cacheable` for performance optimization

### Database Schema Priorities
1. **applications** table (id, name, type, environment, owner)
2. **dependencies** table (source_app_id, target_app_id, type, confidence)
3. **claims** table (id, source_type, raw_data, processed_data, timestamp)
4. **processing_runs** table (id, start_time, end_time, status, summary)

## 🚨 CRITICAL SUCCESS FACTORS

### Data Quality Validation
- Implement comprehensive data validation rules
- Add data quality metrics and reporting
- Include data lineage tracking
- Create automated data quality alerts

### Performance Optimization
- Use connection pooling for database access
- Implement caching for frequently accessed data
- Add bulk processing for large datasets
- Include progress tracking for long-running operations

### Extensibility Design
- Create plugin architecture for new data sources
- Design configurable inference rules
- Build flexible export formats
- Include customizable dashboards

## 🔍 TESTING STRATEGY

### Unit Testing Requirements
- Test coverage >80%
- Mock external dependencies
- Test edge cases and error conditions
- Include performance benchmarks

### Integration Testing
- Test end-to-end data processing pipeline
- Validate data source integrations
- Test conflict resolution scenarios
- Verify export functionality

### Sample Test Data
Generate realistic test data for:
- 150 mock applications
- 1000+ dependency relationships
- Multiple data source formats
- Various conflict scenarios

## 📈 MONITORING & OBSERVABILITY

### Key Metrics to Track
- Processing time per data source
- Accuracy of dependency detection
- Conflict resolution statistics
- System performance metrics
- Data quality scores

### Logging Requirements
- Structured logging with JSON format
- Correlation IDs for tracing
- Performance metrics logging
- Error tracking and alerting

## 🎯 SPECIFIC ASKS FOR COPILOT

When generating code, please:

1. **Always include comprehensive JavaDoc** with examples
2. **Add TODO comments** for areas needing enhancement
3. **Include validation logic** in all data processing methods
4. **Add performance timing** to critical operations
5. **Include error handling** with specific exception types
6. **Add configuration properties** for all hardcoded values
7. **Include unit test stubs** for all public methods
8. **Add logging statements** at key processing points

### Code Generation Priorities
1. Start with core domain models and basic Spring Boot structure
2. Create one complete data source adapter as a template
3. Build the claim processing engine with validation
4. Implement basic conflict resolution algorithm
5. Create simple web dashboard with basic visualization
6. Add database integration with JPA entities
7. Implement basic reporting functionality

### Advanced Features (Phase 2)
- Machine learning for dependency prediction
- Advanced graph algorithms for analysis
- Real-time processing capabilities
- Advanced visualization with D3.js
- REST API for external integrations

## 🏆 SUCCESS CRITERIA

The generated code should:
- ✅ Compile without errors
- ✅ Include comprehensive error handling
- ✅ Follow enterprise coding standards
- ✅ Include proper logging and monitoring
- ✅ Be easily testable and maintainable
- ✅ Support configuration-driven behavior
- ✅ Include performance optimizations
- ✅ Have clear documentation and comments

## 🚀 READY TO START?

Begin with: "Generate the core domain models and Spring Boot application structure for the Application Dependency Matrix system, including comprehensive JavaDoc, validation logic, and unit test stubs."

Focus on creating production-ready, enterprise-grade code that demonstrates sophisticated software architecture principles while being maintainable and extensible.
```

## 🎯 ADDITIONAL CONTEXTUAL PROMPTS

### For Specific Components:

**Data Source Adapter Prompt:**
```
Generate a RouterLogAdapter class that parses Apache/Nginx access logs, extracts dependency information, validates data quality, assigns confidence scores, and converts to standardized Claim objects. Include comprehensive error handling, logging, and unit tests.
```

**Conflict Resolution Prompt:**
```
Create a ConflictResolver class that implements weighted voting algorithms to resolve contradictory dependency claims from multiple sources. Include configurable source priorities, recency weighting, and business rule application.
```

**Dashboard Prompt:**
```
Build a Spring Boot web dashboard with Thymeleaf templates that displays interactive dependency visualizations, search/filter capabilities, and export functionality. Include responsive design and role-based access control.
```
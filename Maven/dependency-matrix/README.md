# Application Dependency Matrix

[![Java](https://img.shields.io/badge/Java-11+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-orange.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Enterprise-red.svg)](LICENSE)

> Enterprise-grade system for discovering, analyzing, and mapping dependencies between applications using multiple data sources including network logs, router logs, and codebase analysis.

## 🎯 Overview

The Application Dependency Matrix is a sophisticated Spring Boot application designed to automatically discover and analyze dependencies between enterprise applications. It ingests data from multiple sources and uses intelligent scoring algorithms to build a comprehensive view of your application ecosystem.

### Key Features

- **🔍 Multi-Source Data Ingestion**: Parses router logs, network logs, and Java codebases
- **🧠 Intelligent Scoring Engine**: Configurable rule-based confidence scoring
- **🏗️ Scalable Architecture**: Clean separation of concerns with Spring Boot
- **📊 RESTful APIs**: Easy integration with external systems
- **🔧 Configurable Processing**: Flexible scoring rules and processing parameters
- **📝 Comprehensive Logging**: Detailed audit trails and debugging support

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Application Dependency Matrix                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌──────────────────┐    ┌─────────────┐ │
│  │   Data Sources  │───▶│  Claim Engine    │───▶│ Dependency  │ │
│  │ • Router Logs   │    │ • Normalization  │    │   Matrix    │ │
│  │ • Network Logs  │    │ • Validation     │    │ • Apps      │ │
│  │ • Codebase      │    │ • Scoring        │    │ • Relations │ │
│  └─────────────────┘    └──────────────────┘    └─────────────┘ │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                      RESTful API Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Parse Router    │  │ Parse Network   │  │ Analyze Code    │ │
│  │ Logs            │  │ Logs            │  │ Dependencies    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

#### 1. **Data Ingestion Layer**
- **LogAndCodebaseParserService**: Handles parsing of multiple data source formats
- **RouterLogAdapter**: Specialized adapter for router log format processing
- Robust error handling with partial result support

#### 2. **Processing Engine**
- **ClaimProcessingEngine**: Central processing hub for claim lifecycle management
- **ScoringRuleEngine**: Pluggable scoring algorithms with configurable rules
- **Validation Engine**: Comprehensive data quality and business rule validation

#### 3. **API Layer**
- **ParserController**: RESTful endpoints for triggering parsing operations
- **RuleManagementController**: Dynamic scoring rule configuration
- Comprehensive error handling and structured responses

#### 4. **Domain Model**
- **Application**: Represents applications in the enterprise ecosystem
- **Dependency**: Represents relationships between applications
- **Claim**: Raw dependency claims from various data sources
- **ConfidenceScore**: Scoring mechanism for dependency reliability

## 🚀 Quick Start

### Prerequisites

- **Java 11+** (OpenJDK or Oracle JDK)
- **Maven 3.6+**
- **Git** (for version control)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd dependency-matrix
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the installation**
   ```bash
   curl http://localhost:8080/api/parse/router-logs
   ```

### Using VS Code Tasks

The project includes pre-configured VS Code tasks for common operations:

```bash
# Build the project
Ctrl+Shift+P → "Tasks: Run Task" → "Build with Maven"

# Run the application
Ctrl+Shift+P → "Tasks: Run Task" → "Run Spring Boot App"
```

## 📖 Usage Guide

### API Endpoints

#### Parse Router Logs
```bash
GET /api/parse/router-logs
```

**Response Format:**
```json
[
  {
    "timestamp": "2025-01-01T10:00:00Z",
    "router": "router01",
    "action": "ACCEPT",
    "src": "10.0.1.100",
    "dst": "10.0.2.200",
    "proto": "TCP",
    "dport": "8080"
  }
]
```

#### Parse Network Logs
```bash
GET /api/parse/network-logs
```

**Response Format:**
```json
[
  {
    "timestamp": "2025-01-01T10:00:00Z",
    "monitor": "monitor01",
    "action": "CONNECT",
    "src": "10.0.1.100",
    "dst": "10.0.2.200",
    "status": "SUCCESS"
  }
]
```

#### Analyze Codebase
```bash
GET /api/parse/codebase
```

**Response Format:**
```json
{
  "UserService": ["DatabaseService", "EmailService"],
  "OrderService": ["UserService", "PaymentService"],
  "PaymentService": ["ExternalPaymentGateway"]
}
```

### Configuration

The application uses `application.yml` for configuration:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration

scoring:
  defaultScore: 0.5
  sourceBaseScore:
    CODEBASE: 0.95
    ROUTER_LOG: 0.85
    API_GATEWAY: 0.80
  processedDataPenalty: 0.2
  minProcessedDataLength: 5
  minScore: 0.0
  maxScore: 1.0
  fieldPenalties:
    id: 0.1
    processedData: 0.15
    rawData: 0.1
    sourceType: 0.1
  maxClaimAgeDays: 30
  oldClaimPenalty: 0.1
```

## 🔧 Development

### Project Structure

```
dependency-matrix/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/enterprise/dependency/
│   │   │       ├── DependencyMatrixApplication.java
│   │   │       ├── config/           # Configuration classes
│   │   │       ├── engine/           # Core processing engines
│   │   │       ├── model/            # Domain models
│   │   │       ├── service/          # Business services
│   │   │       ├── scoring/          # Scoring rule engines
│   │   │       ├── web/              # REST controllers
│   │   │       └── adapter/          # Data adapters
│   │   └── resources/
│   │       ├── application.yml       # Application configuration
│   │       ├── codebases/           # Sample Java files for analysis
│   │       └── logs/                # Sample log files
│   └── test/                        # Unit and integration tests
├── target/                          # Maven build output
├── pom.xml                         # Maven dependencies
└── README.md                       # This file
```

### Key Dependencies

```xml
<!-- Spring Boot Framework -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter</artifactId>
  <version>2.7.18</version>
</dependency>

<!-- Data Persistence -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
  <version>2.7.18</version>
</dependency>

<!-- Database Drivers -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>42.7.3</version>
</dependency>

<!-- Graph Database -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-neo4j</artifactId>
  <version>2.7.18</version>
</dependency>

<!-- Validation -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
  <version>2.7.18</version>
</dependency>

<!-- Code Generation -->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.32</version>
</dependency>
```

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Generate site documentation
mvn site

# Clean install
mvn clean install
```

### Testing

The project includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClaimProcessingEngineTest

# Run with coverage
mvn test jacoco:report
```

## 📊 Business Logic

### Claim Processing Pipeline

1. **Data Ingestion**
   - Router logs parsed for network traffic patterns
   - Network monitoring logs processed for connection attempts
   - Java codebases analyzed for static dependencies

2. **Normalization**
   - Data format standardization
   - Field trimming and canonicalization
   - Timestamp format normalization

3. **Validation**
   - Required field validation
   - Data quality checks
   - Business rule compliance
   - Temporal validation

4. **Scoring**
   - Source-based confidence scoring
   - Data quality penalties
   - Age-based score adjustments
   - Cross-validation scoring

### Scoring Algorithm

The scoring engine considers multiple factors:

- **Source Reliability**: Different data sources have different base scores
- **Data Quality**: Complete, well-formatted data receives higher scores
- **Recency**: Newer claims receive higher confidence scores
- **Cross-Validation**: Claims supported by multiple sources get boosted scores

## 🔄 Recent Updates

### Version 1.0-SNAPSHOT (2025-07-05)

#### ✨ New Features
- **Enhanced Claim Processing**: Comprehensive validation and scoring pipeline
- **Multi-Source Parsing**: Support for router logs, network logs, and codebase analysis
- **Configurable Scoring**: Dynamic rule-based scoring engine
- **RESTful API**: Complete API layer for external integration
- **Comprehensive Logging**: Detailed audit trails and debugging support

#### 🐛 Bug Fixes
- Fixed error handling in parsing operations
- Improved resource management with try-with-resources
- Enhanced validation logic for data quality

#### 🏗️ Technical Improvements
- **Constructor Injection**: Replaced field injection with constructor injection
- **Proper Logging**: Replaced System.err with SLF4J logging
- **Resource Management**: Proper stream handling and resource cleanup
- **Type Safety**: Removed generic wildcard types for better type safety
- **Constants**: Eliminated string duplication with proper constants

#### 📝 Documentation
- **Comprehensive Comments**: Added detailed JavaDoc comments to all classes
- **Architecture Documentation**: Complete system architecture overview
- **API Documentation**: Detailed endpoint documentation with examples
- **Configuration Guide**: Complete configuration reference

## 🔧 Troubleshooting

### Common Issues

1. **Build Failures**
   ```bash
   # Clear Maven cache and rebuild
   mvn dependency:purge-local-repository
   mvn clean install
   ```

2. **Port Already in Use**
   ```bash
   # Change port in application.yml
   server:
     port: 8081
   ```

3. **Memory Issues**
   ```bash
   # Increase JVM memory
   export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
   mvn spring-boot:run
   ```

### Log Locations

- **Application Logs**: Console output (configurable in application.yml)
- **Build Logs**: `target/maven.log`
- **Test Reports**: `target/surefire-reports/`

## 🤝 Contributing

### Development Guidelines

1. **Code Style**: Follow standard Java conventions
2. **Testing**: Write unit tests for all business logic
3. **Documentation**: Update JavaDoc for public APIs
4. **Logging**: Use appropriate log levels (DEBUG, INFO, WARN, ERROR)

### Commit Message Format

```
type(scope): brief description

Detailed explanation of changes
```

**Types**: feat, fix, docs, style, refactor, test, chore

## 📄 License

Copyright © 2025 Enterprise Architecture Team. All rights reserved.

This project is proprietary software developed for enterprise use. Unauthorized distribution is prohibited.

---

## 📞 Support

For support and questions:
- **Email**: architecture-team@enterprise.com
- **Documentation**: [Internal Wiki](http://wiki.enterprise.com/dependency-matrix)
- **Issue Tracking**: [JIRA Project](http://jira.enterprise.com/dependency-matrix)

---

*Last updated: July 5, 2025*

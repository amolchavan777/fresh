# Application Dependency Matrix System

An enterprise-grade full-stack application for analyzing and visualizing software dependencies across multiple data sources.

## 🚀 Quick Start

### One-Command Startup

**macOS/Linux:**
```bash
./start.sh
```

**Windows:**
```cmd
start.bat
```

**To Stop:**
```bash
./stop.sh    # macOS/Linux
stop.bat     # Windows
```

### What the scripts do:
- ✅ Automatically check prerequisites (Java, Maven, Node.js)
- ✅ Handle port conflicts intelligently 
- ✅ Start Spring Boot backend on port 8080
- ✅ Start React frontend on port 5173 (or next available)
- ✅ Verify both services are healthy
- ✅ Display all URLs and endpoints
- ✅ Create logs directory with service logs

### Application URLs (after startup):
- **Main App**: http://localhost:5173
- **Dashboard**: http://localhost:5173/dashboard  
- **Log Parser**: http://localhost:5173/log-parser
- **API Health**: http://localhost:8080/api/dependency-matrix/health

## 📋 Prerequisites

- **Java 8+** (for Spring Boot backend)
- **Maven 3.6+** (for building backend)
- **Node.js 16+** and **npm** (for React frontend)

## 🏗️ System Architecture

### Backend (Spring Boot)
- **Port**: 8080
- **Framework**: Spring Boot 2.7.18
- **Database**: Neo4j (for dependency graphs)
- **Features**: 
  - 5 data source adapters (Router logs, Codebase, API Gateway, CI/CD, Telemetry)
  - Dependency inference engine
  - Conflict resolution
  - RESTful APIs

### Frontend (React + Vite)
- **Port**: 5173 (or auto-assigned)
- **Framework**: React 18 with Vite
- **UI Library**: Material-UI (MUI)
- **Charts**: Recharts
- **Features**:
  - Live dependency dashboard
  - Interactive log parser
  - Rule management interface
  - Dependency visualization

## 📊 Data Sources Supported

1. **Router Logs** - HTTP request/response logs from service routers
2. **Codebase Dependencies** - Build tool dependency files (Maven, npm, etc.)
3. **API Gateway Logs** - JSON-formatted API gateway access logs
4. **CI/CD Logs** - Build and deployment pipeline logs  
5. **Telemetry Data** - Prometheus-style metrics and traces

## 🔧 Manual Setup (Alternative)

If you prefer manual setup, see [HOW_TO_RUN.md](HOW_TO_RUN.md) for detailed instructions.

## 📝 Logs and Troubleshooting

- **Log Location**: `./logs/`
- **Backend Logs**: `./logs/backend.log`
- **Frontend Logs**: `./logs/frontend.log`
- **Process IDs**: Stored in `./logs/*.pid` files

## 🎯 Features

### Live Dashboard
- Real-time dependency metrics
- Source breakdown charts
- Processing history
- System health status

### Log Parser
- Upload and parse custom logs from 5 data sources
- Process sample data for demonstration
- View parsed claims and inferred dependencies

### Rule Management  
- Configure dependency scoring rules
- Manage conflict resolution policies
- Export/import rule configurations

### Dependency Visualization
- Interactive dependency graphs
- Filter by source, type, or confidence
- Export visualizations

## 🏁 System Status

✅ **Fully Operational** - All components integrated and tested
✅ **Live Backend Connectivity** - No offline/mock data
✅ **5 Data Sources** - Complete enterprise data pipeline  
✅ **End-to-End Processing** - From raw logs to dependency graphs

---

**Start the system with one command and explore enterprise dependency analysis!** 🚀
# 🚀 How to Run the Application Dependency Matrix System

## 🎯 Quick Start with Scripts (Recommended)

### For macOS/Linux:
```bash
# Make scripts executable (one time only)
chmod +x start.sh stop.sh

# Start the entire system
./start.sh

# Stop the system
./stop.sh
```

### For Windows:
```cmd
# Start the entire system
start.bat

# Stop the system
stop.bat
```

The startup script will:
- ✅ Check all prerequisites (Java, Maven, Node.js, npm)
- ✅ Handle port conflicts automatically
- ✅ Start backend and frontend services
- ✅ Wait for services to be ready
- ✅ Verify system health
- ✅ Display all URLs and endpoints
- ✅ Optionally open the app in your browser

## Manual Setup Guide

### Prerequisites
- **Java 8+** (for Spring Boot backend)
- **Maven 3.6+** (for building the backend)
- **Node.js 16+** and **npm** (for React frontend)

## Method 1: Standard Startup (Recommended)

### Step 1: Start the Backend
```bash
cd Maven/dependency-matrix
mvn spring-boot:run
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.7.18)

...
Tomcat started on port(s): 8080 (http) with context path ''
Started DependencyMatrixApplication in X.XXX seconds
```

### Step 2: Start the Frontend (in a new terminal)
```bash
cd Maven/Front-end
npm install  # Only needed first time
npm run dev
```

**Expected Output:**
```
  VITE v7.0.2  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

### Step 3: Access the Application
Open your browser and navigate to: **http://localhost:5173**

## Method 2: If Ports Are In Use

### Check Running Services
```bash
# Check what's running on the ports
lsof -i :8080 -i :5173

# Kill processes if needed (replace PID with actual process ID)
kill <PID>
```

### Alternative Ports
If the default ports are occupied:
- **Frontend** will automatically try ports 5174, 5175, etc.
- **Backend** can be configured to use a different port by adding to `application.properties`:
  ```properties
  server.port=8081
  ```

## Method 3: Production Build

### Build Frontend for Production
```bash
cd Maven/Front-end
npm run build
```

### Package Backend
```bash
cd Maven/dependency-matrix
mvn clean package
java -jar target/dependency-matrix-1.0-SNAPSHOT.jar
```

## 🌐 Application URLs

Once both services are running:

### Main Application
- **Frontend**: http://localhost:5173 (or 5174 if 5173 is in use)
- **Backend API**: http://localhost:8080

### Available Pages
- **Home**: http://localhost:5173/
- **Dashboard**: http://localhost:5173/dashboard
- **Log Parser**: http://localhost:5173/log-parser
- **Rule Management**: http://localhost:5173/rule-management
- **Dependency Visualization**: http://localhost:5173/dependency-visualization

### Backend API Endpoints
- **Health Check**: http://localhost:8080/api/dependency-matrix/health
- **Summary**: http://localhost:8080/api/dependency-matrix/summary
- **Sample Data**: http://localhost:8080/api/dependency-matrix/sample
- **Process Custom Data**: http://localhost:8080/api/dependency-matrix/process

## ✅ Verification Steps

### 1. Backend Health Check
```bash
curl http://localhost:8080/api/dependency-matrix/health
```
**Expected Response:**
```json
{"status":"healthy","service":"DependencyMatrixService","timestamp":"..."}
```

### 2. Frontend Proxy Test
```bash
curl http://localhost:5173/api/dependency-matrix/health
```
**Should return the same response as above**

### 3. Sample Data Test
```bash
curl http://localhost:5173/api/dependency-matrix/sample
```
**Should return dependency matrix with ~30 dependencies**

## 🔧 Troubleshooting

### Common Issues

#### Port Already in Use
**Error**: `Port 8080 was already in use`
**Solution**:
```bash
# Find and kill the process using the port
lsof -i :8080
kill <PID>
```

#### Frontend Build Issues
**Error**: `npm command not found` or `package.json not found`
**Solution**:
```bash
# Make sure you're in the right directory
cd Maven/Front-end
# Install dependencies
npm install
```

#### Backend Compilation Issues
**Error**: Maven compilation failures
**Solution**:
```bash
# Clean and rebuild
cd Maven/dependency-matrix
mvn clean compile
mvn spring-boot:run
```

#### CORS Issues
**Error**: Frontend can't connect to backend
**Solution**: The backend is configured with CORS for ports 5173-5176. If using a different port, update `@CrossOrigin` annotations in the controllers.

### Performance Notes
- **Backend startup**: ~3-5 seconds
- **Frontend startup**: ~1-2 seconds
- **Sample data processing**: ~100ms
- **Custom data processing**: ~200-500ms depending on data size

## 📊 System Features

Once running, you can:
1. **View live dashboard** with real-time dependency metrics
2. **Parse custom logs** from 5 different data sources:
   - Router logs
   - Codebase dependencies
   - API Gateway logs
   - CI/CD logs
   - Telemetry data
3. **Manage rules** for dependency scoring
4. **Visualize dependencies** in graph format
5. **Process sample data** for demonstration

## 🎯 Current Status
- ✅ Backend: Running and stable
- ✅ Frontend: Running with live backend connectivity
- ✅ All 5 data sources integrated
- ✅ Sample and custom data processing working
- ✅ Real-time dashboard with live metrics

The system is **fully operational** and ready for use!

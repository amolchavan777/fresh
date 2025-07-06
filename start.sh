#!/bin/bash

# Application Dependency Matrix System - Startup Script
# This script starts both the backend and frontend services

set -e  # Exit on any error

echo "🚀 Starting Application Dependency Matrix System..."
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to kill process on port
kill_port() {
    local port=$1
    echo -e "${YELLOW}Killing process on port $port...${NC}"
    local pid=$(lsof -ti:$port)
    if [ ! -z "$pid" ]; then
        kill -9 $pid
        sleep 2
        echo -e "${GREEN}Process on port $port killed.${NC}"
    fi
}

# Check prerequisites
echo -e "${BLUE}Checking prerequisites...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    echo "Please install Java 8+ and try again"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed or not in PATH${NC}"
    echo "Please install Maven 3.6+ and try again"
    exit 1
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed or not in PATH${NC}"
    echo "Please install Node.js 16+ and try again"
    exit 1
fi

# Check npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm is not installed or not in PATH${NC}"
    echo "Please install npm and try again"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites are installed${NC}"

# Check if ports are in use and offer to kill processes
if check_port 8080; then
    echo -e "${YELLOW}⚠️  Port 8080 is already in use${NC}"
    read -p "Do you want to kill the process and continue? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kill_port 8080
    else
        echo -e "${RED}❌ Cannot start backend on port 8080. Exiting.${NC}"
        exit 1
    fi
fi

if check_port 5173; then
    echo -e "${YELLOW}⚠️  Port 5173 is already in use${NC}"
    read -p "Do you want to kill the process and continue? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kill_port 5173
    else
        echo -e "${YELLOW}Frontend will try to use an alternative port (5174, 5175, etc.)${NC}"
    fi
fi

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
MAVEN_DIR="$SCRIPT_DIR/Maven"
BACKEND_DIR="$MAVEN_DIR/dependency-matrix"
FRONTEND_DIR="$MAVEN_DIR/Front-end"

# Check if directories exist
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}❌ Backend directory not found: $BACKEND_DIR${NC}"
    exit 1
fi

if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}❌ Frontend directory not found: $FRONTEND_DIR${NC}"
    exit 1
fi

# Create log directory
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

echo -e "${BLUE}Starting services...${NC}"

# Start backend in background
echo -e "${YELLOW}🔧 Starting Spring Boot backend...${NC}"
cd "$BACKEND_DIR"

# Start backend and capture PID
mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!

echo "Backend PID: $BACKEND_PID"
echo $BACKEND_PID > "$LOG_DIR/backend.pid"

# Wait for backend to start
echo -e "${YELLOW}⏳ Waiting for backend to start...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/api/dependency-matrix/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend is running on http://localhost:8080${NC}"
        break
    fi
    
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Backend failed to start within 30 seconds${NC}"
        echo "Check logs at: $LOG_DIR/backend.log"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    
    echo -n "."
    sleep 1
done
echo

# Start frontend
echo -e "${YELLOW}🔧 Starting React frontend...${NC}"
cd "$FRONTEND_DIR"

# Install npm dependencies if needed
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📦 Installing npm dependencies...${NC}"
    npm install
fi

# Start frontend and capture PID
npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo "Frontend PID: $FRONTEND_PID"
echo $FRONTEND_PID > "$LOG_DIR/frontend.pid"

# Wait for frontend to start
echo -e "${YELLOW}⏳ Waiting for frontend to start...${NC}"
FRONTEND_PORT=""
for i in {1..20}; do
    # Check multiple possible ports
    for port in 5173 5174 5175 5176; do
        if curl -s http://localhost:$port > /dev/null 2>&1; then
            FRONTEND_PORT=$port
            echo -e "${GREEN}✅ Frontend is running on http://localhost:$port${NC}"
            break 2
        fi
    done
    
    if [ $i -eq 20 ]; then
        echo -e "${RED}❌ Frontend failed to start within 20 seconds${NC}"
        echo "Check logs at: $LOG_DIR/frontend.log"
        kill $FRONTEND_PID 2>/dev/null || true
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    
    echo -n "."
    sleep 1
done
echo

# Final verification
echo -e "${BLUE}🔍 Verifying system health...${NC}"

# Test backend health
if curl -s http://localhost:8080/api/dependency-matrix/health | grep -q "healthy"; then
    echo -e "${GREEN}✅ Backend health check passed${NC}"
else
    echo -e "${RED}❌ Backend health check failed${NC}"
fi

# Test frontend proxy
if curl -s http://localhost:$FRONTEND_PORT/api/dependency-matrix/health | grep -q "healthy"; then
    echo -e "${GREEN}✅ Frontend proxy test passed${NC}"
else
    echo -e "${RED}❌ Frontend proxy test failed${NC}"
fi

echo
echo -e "${GREEN}🎉 Application Dependency Matrix System is now running!${NC}"
echo "=================================================="
echo -e "${BLUE}📱 Frontend:${NC} http://localhost:$FRONTEND_PORT"
echo -e "${BLUE}🔧 Backend API:${NC} http://localhost:8080"
echo
echo -e "${BLUE}📋 Available Pages:${NC}"
echo "   • Dashboard: http://localhost:$FRONTEND_PORT/dashboard"
echo "   • Log Parser: http://localhost:$FRONTEND_PORT/log-parser"
echo "   • Rule Management: http://localhost:$FRONTEND_PORT/rule-management"
echo "   • Dependency Visualization: http://localhost:$FRONTEND_PORT/dependency-visualization"
echo
echo -e "${BLUE}📊 API Endpoints:${NC}"
echo "   • Health: http://localhost:8080/api/dependency-matrix/health"
echo "   • Summary: http://localhost:8080/api/dependency-matrix/summary"
echo "   • Sample Data: http://localhost:8080/api/dependency-matrix/sample"
echo
echo -e "${YELLOW}📝 Process Information:${NC}"
echo "   • Backend PID: $BACKEND_PID (logs: $LOG_DIR/backend.log)"
echo "   • Frontend PID: $FRONTEND_PID (logs: $LOG_DIR/frontend.log)"
echo
echo -e "${YELLOW}🛑 To stop the services, run:${NC} ./stop.sh"
echo

# Open browser (optional)
read -p "Would you like to open the application in your default browser? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v open &> /dev/null; then
        open "http://localhost:$FRONTEND_PORT"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "http://localhost:$FRONTEND_PORT"
    else
        echo "Please open http://localhost:$FRONTEND_PORT in your browser"
    fi
fi

echo -e "${GREEN}Startup completed successfully! 🚀${NC}"

#!/bin/bash

# Application Dependency Matrix System - Stop Script
# This script stops both the backend and frontend services

echo "🛑 Stopping Application Dependency Matrix System..."
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
LOG_DIR="$SCRIPT_DIR/logs"

# Function to kill process by PID
kill_process() {
    local pid=$1
    local name=$2
    
    if [ ! -z "$pid" ] && kill -0 $pid 2>/dev/null; then
        echo -e "${YELLOW}Stopping $name (PID: $pid)...${NC}"
        kill -TERM $pid
        
        # Wait for graceful shutdown
        for i in {1..10}; do
            if ! kill -0 $pid 2>/dev/null; then
                echo -e "${GREEN}✅ $name stopped gracefully${NC}"
                return 0
            fi
            sleep 1
        done
        
        # Force kill if still running
        echo -e "${YELLOW}Force stopping $name...${NC}"
        kill -9 $pid 2>/dev/null || true
        echo -e "${GREEN}✅ $name force stopped${NC}"
    else
        echo -e "${BLUE}ℹ️  $name is not running${NC}"
    fi
}

# Function to kill processes on specific ports
kill_port_processes() {
    local port=$1
    local name=$2
    
    local pids=$(lsof -ti:$port 2>/dev/null || true)
    if [ ! -z "$pids" ]; then
        echo -e "${YELLOW}Stopping processes on port $port ($name)...${NC}"
        echo $pids | xargs kill -TERM 2>/dev/null || true
        sleep 2
        
        # Force kill if still running
        local remaining_pids=$(lsof -ti:$port 2>/dev/null || true)
        if [ ! -z "$remaining_pids" ]; then
            echo $remaining_pids | xargs kill -9 2>/dev/null || true
        fi
        echo -e "${GREEN}✅ $name processes stopped${NC}"
    else
        echo -e "${BLUE}ℹ️  No processes running on port $port ($name)${NC}"
    fi
}

# Stop services using PID files
if [ -f "$LOG_DIR/backend.pid" ]; then
    BACKEND_PID=$(cat "$LOG_DIR/backend.pid")
    kill_process $BACKEND_PID "Backend"
    rm -f "$LOG_DIR/backend.pid"
fi

if [ -f "$LOG_DIR/frontend.pid" ]; then
    FRONTEND_PID=$(cat "$LOG_DIR/frontend.pid")
    kill_process $FRONTEND_PID "Frontend"
    rm -f "$LOG_DIR/frontend.pid"
fi

# Also kill any remaining processes on the ports
kill_port_processes 8080 "Backend"
kill_port_processes 5173 "Frontend (5173)"
kill_port_processes 5174 "Frontend (5174)"
kill_port_processes 5175 "Frontend (5175)"
kill_port_processes 5176 "Frontend (5176)"

# Find and kill any remaining Java Spring Boot processes
echo -e "${BLUE}Checking for remaining Spring Boot processes...${NC}"
SPRING_PIDS=$(ps aux | grep "spring-boot:run" | grep -v grep | awk '{print $2}' || true)
if [ ! -z "$SPRING_PIDS" ]; then
    echo -e "${YELLOW}Stopping remaining Spring Boot processes...${NC}"
    echo $SPRING_PIDS | xargs kill -TERM 2>/dev/null || true
    sleep 2
    
    # Force kill if still running
    REMAINING_SPRING_PIDS=$(ps aux | grep "spring-boot:run" | grep -v grep | awk '{print $2}' || true)
    if [ ! -z "$REMAINING_SPRING_PIDS" ]; then
        echo $REMAINING_SPRING_PIDS | xargs kill -9 2>/dev/null || true
    fi
    echo -e "${GREEN}✅ Spring Boot processes stopped${NC}"
else
    echo -e "${BLUE}ℹ️  No Spring Boot processes found${NC}"
fi

# Find and kill any remaining Node.js Vite processes
echo -e "${BLUE}Checking for remaining Vite processes...${NC}"
VITE_PIDS=$(ps aux | grep "vite" | grep -v grep | awk '{print $2}' || true)
if [ ! -z "$VITE_PIDS" ]; then
    echo -e "${YELLOW}Stopping remaining Vite processes...${NC}"
    echo $VITE_PIDS | xargs kill -TERM 2>/dev/null || true
    sleep 2
    
    # Force kill if still running
    REMAINING_VITE_PIDS=$(ps aux | grep "vite" | grep -v grep | awk '{print $2}' || true)
    if [ ! -z "$REMAINING_VITE_PIDS" ]; then
        echo $REMAINING_VITE_PIDS | xargs kill -9 2>/dev/null || true
    fi
    echo -e "${GREEN}✅ Vite processes stopped${NC}"
else
    echo -e "${BLUE}ℹ️  No Vite processes found${NC}"
fi

# Verify ports are free
echo -e "${BLUE}🔍 Verifying ports are free...${NC}"

for port in 8080 5173 5174 5175 5176; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Port $port is still in use${NC}"
    else
        echo -e "${GREEN}✅ Port $port is free${NC}"
    fi
done

echo
echo -e "${GREEN}🏁 Application Dependency Matrix System stopped successfully!${NC}"
echo "=================================================="
echo -e "${BLUE}📝 Log files are preserved in: $LOG_DIR${NC}"
echo -e "${BLUE}🚀 To start again, run:${NC} ./start.sh"
echo

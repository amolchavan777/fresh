#!/bin/bash

# Dependency Matrix Application Startup Script
echo "🚀 Starting Dependency Matrix Application..."
echo "============================================"

# Check if required tools are available
command -v mvn >/dev/null 2>&1 || { echo "❌ Maven is required but not installed."; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "❌ npm is required but not installed."; exit 1; }

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/dependency-matrix"
FRONTEND_DIR="$SCRIPT_DIR/Front-end"

# Function to stop background processes on exit
cleanup() {
    echo "🛑 Stopping servers..."
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "vite" 2>/dev/null || true
    echo "✅ Cleanup completed"
    exit 0
}

# Setup signal handling
trap cleanup SIGINT SIGTERM

# Kill any existing processes on our ports
echo "🧹 Cleaning up any existing processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:5173 | xargs kill -9 2>/dev/null || true

# Start backend server
echo "🏗️  Starting Spring Boot backend server..."
cd "$BACKEND_DIR"
mvn org.springframework.boot:spring-boot-maven-plugin:run > backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/dependency-matrix/health >/dev/null 2>&1; then
        echo "✅ Backend server is running on http://localhost:8080"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo "❌ Backend failed to start within 60 seconds"
        exit 1
    fi
done

# Install frontend dependencies if needed
echo "📦 Checking frontend dependencies..."
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

# Start frontend server
echo "🎨 Starting React frontend server..."
npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for frontend to start
echo "⏳ Waiting for frontend to start..."
for i in {1..15}; do
    if curl -s http://localhost:5173 >/dev/null 2>&1; then
        echo "✅ Frontend server is running on http://localhost:5173"
        break
    fi
    sleep 2
    if [ $i -eq 15 ]; then
        echo "❌ Frontend failed to start within 30 seconds"
        exit 1
    fi
done

echo ""
echo "🎉 Dependency Matrix Application is ready!"
echo "============================================"
echo "📊 Frontend:  http://localhost:5173"
echo "🔧 Backend:   http://localhost:8080"
echo "📚 API Docs:  http://localhost:8080/api/dependency-matrix/health"
echo ""
echo "Available API endpoints:"
echo "  GET  /api/dependency-matrix/sample"
echo "  POST /api/dependency-matrix/process"
echo "  GET  /api/dependency-matrix/summary"
echo "  GET  /api/dependency-matrix/health"
echo ""
echo "Press Ctrl+C to stop all servers"
echo "============================================"

# Keep script running and wait for user to stop
wait $BACKEND_PID $FRONTEND_PID

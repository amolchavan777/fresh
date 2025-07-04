#!/bin/bash
# Start both backend and frontend for rule management UI

BACKEND_DIR="../dependency-matrix"
FRONTEND_DIR="$(pwd)"

# Start backend
cd "$BACKEND_DIR"
echo "Starting Spring Boot backend..."
mvn spring-boot:run &
BACKEND_PID=$!
cd "$FRONTEND_DIR"

# Wait a bit for backend to start
sleep 10

echo "Starting React frontend..."
npm run dev

# When frontend stops, kill backend
kill $BACKEND_PID

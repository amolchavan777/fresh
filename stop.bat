@echo off
:: Application Dependency Matrix System - Stop Script (Windows)
:: This script stops both the backend and frontend services

echo 🛑 Stopping Application Dependency Matrix System...
echo ==================================================

:: Kill processes on specific ports
echo Stopping Backend (port 8080)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo Stopping Frontend (port 5173)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo Stopping Frontend (port 5174)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5174') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo Stopping Frontend (port 5175)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5175') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo Stopping Frontend (port 5176)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5176') do (
    taskkill /PID %%a /F >nul 2>&1
)

:: Kill any remaining Maven/Spring Boot processes
echo Stopping remaining Spring Boot processes...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq Backend Server*" >nul 2>&1

:: Kill any remaining Node/Vite processes
echo Stopping remaining Vite processes...
taskkill /F /IM node.exe /FI "WINDOWTITLE eq Frontend Server*" >nul 2>&1

:: Wait a moment for processes to stop
timeout /t 2 /nobreak >nul

echo.
echo 🏁 Application Dependency Matrix System stopped successfully!
echo ==================================================
echo 🚀 To start again, run: start.bat
echo.
pause

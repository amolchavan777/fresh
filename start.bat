@echo off
:: Application Dependency Matrix System - Startup Script (Windows)
:: This script starts both the backend and frontend services

setlocal enabledelayedexpansion

echo 🚀 Starting Application Dependency Matrix System...
echo ==================================================

:: Check prerequisites
echo Checking prerequisites...

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed or not in PATH
    echo Please install Java 8+ and try again
    pause
    exit /b 1
)

:: Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven is not installed or not in PATH
    echo Please install Maven 3.6+ and try again
    pause
    exit /b 1
)

:: Check Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is not installed or not in PATH
    echo Please install Node.js 16+ and try again
    pause
    exit /b 1
)

:: Check npm
npm -v >nul 2>&1
if errorlevel 1 (
    echo ❌ npm is not installed or not in PATH
    echo Please install npm and try again
    pause
    exit /b 1
)

echo ✅ All prerequisites are installed

:: Get current directory
set SCRIPT_DIR=%~dp0
set MAVEN_DIR=%SCRIPT_DIR%Maven
set BACKEND_DIR=%MAVEN_DIR%\dependency-matrix
set FRONTEND_DIR=%MAVEN_DIR%\Front-end

:: Check if directories exist
if not exist "%BACKEND_DIR%" (
    echo ❌ Backend directory not found: %BACKEND_DIR%
    pause
    exit /b 1
)

if not exist "%FRONTEND_DIR%" (
    echo ❌ Frontend directory not found: %FRONTEND_DIR%
    pause
    exit /b 1
)

:: Create log directory
set LOG_DIR=%SCRIPT_DIR%logs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo Starting services...

:: Start backend
echo 🔧 Starting Spring Boot backend...
cd /d "%BACKEND_DIR%"

:: Start backend in background
start "Backend Server" /min cmd /c "mvn spring-boot:run > "%LOG_DIR%\backend.log" 2>&1"

:: Wait for backend to start
echo ⏳ Waiting for backend to start...
set /a counter=0
:backend_wait
timeout /t 1 /nobreak >nul
set /a counter+=1

:: Try to connect to backend
curl -s http://localhost:8080/api/dependency-matrix/health >nul 2>&1
if !errorlevel! equ 0 (
    echo ✅ Backend is running on http://localhost:8080
    goto backend_ready
)

if !counter! geq 30 (
    echo ❌ Backend failed to start within 30 seconds
    echo Check logs at: %LOG_DIR%\backend.log
    pause
    exit /b 1
)

echo|set /p="."
goto backend_wait

:backend_ready

:: Start frontend
echo 🔧 Starting React frontend...
cd /d "%FRONTEND_DIR%"

:: Install npm dependencies if needed
if not exist "node_modules" (
    echo 📦 Installing npm dependencies...
    npm install
)

:: Start frontend in background
start "Frontend Server" /min cmd /c "npm run dev > "%LOG_DIR%\frontend.log" 2>&1"

:: Wait for frontend to start
echo ⏳ Waiting for frontend to start...
set /a counter=0
set FRONTEND_PORT=

:frontend_wait
timeout /t 1 /nobreak >nul
set /a counter+=1

:: Check multiple possible ports
for %%p in (5173 5174 5175 5176) do (
    curl -s http://localhost:%%p >nul 2>&1
    if !errorlevel! equ 0 (
        set FRONTEND_PORT=%%p
        echo ✅ Frontend is running on http://localhost:%%p
        goto frontend_ready
    )
)

if !counter! geq 20 (
    echo ❌ Frontend failed to start within 20 seconds
    echo Check logs at: %LOG_DIR%\frontend.log
    pause
    exit /b 1
)

echo|set /p="."
goto frontend_wait

:frontend_ready

:: Final verification
echo 🔍 Verifying system health...

:: Test backend health
curl -s http://localhost:8080/api/dependency-matrix/health | findstr "healthy" >nul
if !errorlevel! equ 0 (
    echo ✅ Backend health check passed
) else (
    echo ❌ Backend health check failed
)

:: Test frontend proxy
curl -s http://localhost:!FRONTEND_PORT!/api/dependency-matrix/health | findstr "healthy" >nul
if !errorlevel! equ 0 (
    echo ✅ Frontend proxy test passed
) else (
    echo ❌ Frontend proxy test failed
)

echo.
echo 🎉 Application Dependency Matrix System is now running!
echo ==================================================
echo 📱 Frontend: http://localhost:!FRONTEND_PORT!
echo 🔧 Backend API: http://localhost:8080
echo.
echo 📋 Available Pages:
echo    • Dashboard: http://localhost:!FRONTEND_PORT!/dashboard
echo    • Log Parser: http://localhost:!FRONTEND_PORT!/log-parser
echo    • Rule Management: http://localhost:!FRONTEND_PORT!/rule-management
echo    • Dependency Visualization: http://localhost:!FRONTEND_PORT!/dependency-visualization
echo.
echo 📊 API Endpoints:
echo    • Health: http://localhost:8080/api/dependency-matrix/health
echo    • Summary: http://localhost:8080/api/dependency-matrix/summary
echo    • Sample Data: http://localhost:8080/api/dependency-matrix/sample
echo.
echo 🛑 To stop the services, run: stop.bat
echo.

:: Ask to open browser
set /p "choice=Would you like to open the application in your default browser? (y/n): "
if /i "!choice!"=="y" (
    start http://localhost:!FRONTEND_PORT!
)

echo Startup completed successfully! 🚀
pause

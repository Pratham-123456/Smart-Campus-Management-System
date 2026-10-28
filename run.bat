@echo off
setlocal enabledelayedexpansion
title Smart Campus Management System
color 0B

echo ======================================================================
echo           XYZ UNIVERSITY - ACADEMIC SERVICES PORTAL
echo ======================================================================
echo.

:: 1. Check Docker Desktop & Daemon
echo [1/4] Checking Docker service...
docker info >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERROR] Docker Desktop is not running!
    echo Please start Docker Desktop from your Start Menu and wait until it is ready.
    echo.
    pause
    exit /b 1
)

:: 2. Check Oracle 26ai Docker Container
echo [2/4] Checking Oracle 26ai database container 'oracle-db'...
docker inspect -f "{{.State.Running}}" oracle-db 2>nul | find "true" >nul
if errorlevel 1 (
    echo [INFO] Starting Oracle container 'oracle-db'...
    docker start oracle-db
    echo Waiting 15 seconds for database service to initialize...
    timeout /t 15 /nobreak >nul
) else (
    echo [OK] Oracle Database container is running.
)

:: 3. Verify Database Credentials
echo.
echo [3/4] Verifying database credentials...
if "%CAMPUS_DB_PASSWORD%"=="" (
    set "CAMPUS_DB_PASSWORD=<your-db-password>"
)
echo [OK] Database credentials loaded.

:: 4. Launch Spring Boot Web Server
echo.
echo [4/4] Launching Spring Boot Web Server on port 8080...
cd /d "%~dp0SmartCampus"

:: Open browser after a brief delay in the background
start "" cmd /c "timeout /t 4 /nobreak >nul & start http://localhost:8080/"

if exist "target\smart-campus-1.0.0.jar" (
    java "-Dspring.datasource.password=%CAMPUS_DB_PASSWORD%" -jar "target\smart-campus-1.0.0.jar"
) else (
    echo [INFO] Prebuilt JAR not found, building and starting via Maven...
    call .\mvnw.cmd spring-boot:run
)

echo.
echo Server stopped.
pause

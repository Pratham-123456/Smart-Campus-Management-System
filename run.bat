@echo off
title Smart Campus Management System
color 0B

echo ======================================================================
echo           XYZ UNIVERSITY — ACADEMIC SERVICES PORTAL
echo ======================================================================
echo.

:: 1. Check Docker Container
echo [1/4] Checking Oracle 26ai Docker Container...
docker inspect -f "{{.State.Running}}" oracle-db 2>nul | find "true" >nul
if errorlevel 1 (
    echo [INFO] Starting Oracle container 'oracle-db'...
    docker start oracle-db
    timeout /t 5 >nul
) else (
    echo [OK] Oracle Database container is healthy and running.
)

:: 2. Check Database Password
echo.
echo [2/4] Verifying database credentials...
if "%CAMPUS_DB_PASSWORD%"=="" (
    set "CAMPUS_DB_PASSWORD=<your-db-password>"
)
echo [OK] Database credentials loaded.

:: 3. Launch Spring Boot Application
echo.
echo [3/4] Launching Spring Boot Web Server on port 8080...
cd /d "%~dp0SmartCampus"

start "" http://localhost:8080/

if exist "target\smart-campus-1.0.0.jar" (
    java "-Dspring.datasource.password=%CAMPUS_DB_PASSWORD%" -jar "target\smart-campus-1.0.0.jar"
) else (
    call .\mvnw.cmd spring-boot:run
)

pause

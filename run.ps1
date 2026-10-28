# XYZ University Academic Services Portal - PowerShell Launcher
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "             XYZ UNIVERSITY — ACADEMIC SERVICES PORTAL" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check Oracle Docker Container
Write-Host "[1/4] Checking Oracle Docker Container..." -ForegroundColor Gray
$isRunning = docker inspect -f '{{.State.Running}}' oracle-db 2>$null
if ($isRunning -ne "true") {
    Write-Host "[INFO] Starting container 'oracle-db'..." -ForegroundColor Yellow
    docker start oracle-db
    Start-Sleep -Seconds 5
} else {
    Write-Host "[OK] Oracle Database 26ai Free is running." -ForegroundColor Green
}

# 2. Check Database Password
Write-Host "[2/4] Verifying database credentials..." -ForegroundColor Gray
if ([string]::IsNullOrWhiteSpace($env:CAMPUS_DB_PASSWORD)) {
    $env:CAMPUS_DB_PASSWORD = [System.Environment]::GetEnvironmentVariable("CAMPUS_DB_PASSWORD", "User")
    if ([string]::IsNullOrWhiteSpace($env:CAMPUS_DB_PASSWORD)) {
        # Never hardcode the password: prompt (input hidden). Save it once with: setx CAMPUS_DB_PASSWORD "..."
        $secure = Read-Host "Enter the Oracle password for campus_app" -AsSecureString
        $env:CAMPUS_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password
    }
    if ([string]::IsNullOrWhiteSpace($env:CAMPUS_DB_PASSWORD)) {
        Write-Host "[ERROR] No database password provided." -ForegroundColor Red
        exit 1
    }
}
Write-Host "[OK] Database credentials loaded." -ForegroundColor Green

# 3. Launch Web Browser
Write-Host "[3/4] Launching browser to http://localhost:8080/..." -ForegroundColor Gray
Start-Process "http://localhost:8080/"

# 4. Launch Spring Boot
Write-Host "[4/4] Starting Spring Boot application..." -ForegroundColor Green
$smartCampusDir = Join-Path $PSScriptRoot "SmartCampus"
Set-Location $smartCampusDir

$jarPath = Join-Path $smartCampusDir "target\smart-campus-1.0.0.jar"
if (Test-Path $jarPath) {
    java "-Dspring.datasource.password=$env:CAMPUS_DB_PASSWORD" -jar $jarPath
} else {
    & ".\mvnw.cmd" spring-boot:run
}

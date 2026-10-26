[CmdletBinding()]
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " XYZ University -- Multi-Role Account Provisioning Utility" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "Notice: Assigning temporary development password '1234' (BCrypt hashed)" -ForegroundColor Yellow
Write-Host ""

$dbPassword = [Environment]::GetEnvironmentVariable("CAMPUS_DB_PASSWORD", "User")
if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    $dbPassword = [Environment]::GetEnvironmentVariable("CAMPUS_DB_PASSWORD", "Process")
}

if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    $cred = Get-Credential -UserName "campus_app" -Message "Enter Oracle DB password for campus_app"
    $dbPassword = $cred.GetNetworkCredential().Password
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir
$sqlFile = Join-Path $projectRoot "sql\04_authentication_migration.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Error "Migration file not found: $sqlFile"
    exit 1
}

Write-Host "Executing authentication migration and provisioning..." -ForegroundColor Green
$sqlContent = Get-Content -Path $sqlFile -Raw
$fullScript = $sqlContent + [System.Environment]::NewLine + "EXIT;" + [System.Environment]::NewLine
$connStr = "campus_app/$dbPassword@localhost:1521/FREEPDB1"
$output = $fullScript | docker exec -i oracle-db sqlplus -s "$connStr"
Write-Host $output
Write-Host "Multi-role account provisioning completed successfully." -ForegroundColor Cyan

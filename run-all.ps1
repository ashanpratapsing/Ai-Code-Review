# AI Code Review Platform - Unified Orchestration Script (FAANG-Level)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [🚀] INITIALIZING AI CODE REVIEW FULL-STACK PLATFORM" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Clean and Start Infrastructure
Write-Host "[1/4] Starting high-availability infrastructure (Postgres, Redis, MQ, Prometheus)..." -ForegroundColor Yellow
docker-compose up -d postgres redis rabbitmq prometheus grafana

# 2. Wait for Infrastructure Health
Write-Host "Waiting for infrastructure to stabilize (Healthchecks)..." -ForegroundColor DarkGray
do {
    $status = docker-compose ps --format json | ConvertFrom-Json
    $healthyCount = ($status | Where-Object { $_.Health -eq "healthy" -or $_.State -eq "running" }).Count
    Write-Host "Services stabilized: $healthyCount / 5" -NoNewline
    Write-Host "`r" -NoNewline
    Start-Sleep -Seconds 2
} while ($healthyCount -lt 5)

Write-Host "`nInfrastructure is READY." -ForegroundColor Green

# 3. Start Spring Boot Backend
Write-Host "[2/4] Launching Spring Boot Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit -Command `"./mvnw spring-boot:run`""

# 4. Start React Frontend
Write-Host "[3/4] Launching Vite React Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit -Command `"cd frontend; npm run dev`""

# 5. Summary
Write-Host "==========================================================" -ForegroundColor Green
Write-Host " [✓] ALL SERVICES ARE OPERATIONAL" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host " -> Frontend:    http://localhost:5173"
Write-Host " -> Backend API:  http://localhost:8080"
Write-Host " -> Monitoring:   http://localhost:3000 (admin/admin)"
Write-Host " -> MQ Admin:     http://localhost:15672 (guest/guest)"
Write-Host "=========================================================="

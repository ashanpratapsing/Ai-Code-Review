Write-Host "=========================================="
Write-Host " Starting AI Code Review Platform "
Write-Host "=========================================="

Write-Host "1. Starting MySQL Database via Docker Compose..."
docker-compose up -d mysql

Write-Host "Waiting 10 seconds for MySQL to be ready..."
Start-Sleep -Seconds 10

Write-Host "2. Starting Spring Boot Backend in a new window..."
Start-Process powershell -ArgumentList "-NoExit -Command `"mvnw.cmd spring-boot:run`""

Write-Host "3. Starting Vite React Frontend in a new window..."
Start-Process powershell -ArgumentList "-NoExit -Command `"cd frontend; npm run dev`""

Write-Host "=========================================="
Write-Host " Services are starting up! "
Write-Host " - Backend: http://localhost:8080"
Write-Host " - Frontend: http://localhost:5173"
Write-Host "=========================================="

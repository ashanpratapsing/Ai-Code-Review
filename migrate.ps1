# migrate.ps1
$ErrorActionPreference = "Stop"

Write-Host "Creating module directories..."
mkdir core-libs/src/main/java/com/student/demo/entity -Force
mkdir core-libs/src/main/java/com/student/demo/repository -Force
mkdir core-libs/src/main/java/com/student/demo/config -Force
mkdir core-libs/src/main/resources/db/migration -Force

mkdir api-service/src/main/java/com/student/demo/controller -Force
mkdir api-service/src/main/java/com/student/demo/security -Force
mkdir api-service/src/main/java/com/student/demo/service -Force
mkdir api-service/src/main/java/com/student/demo/config -Force
mkdir api-service/src/main/resources -Force

mkdir worker-service/src/main/java/com/student/demo/mq -Force
mkdir worker-service/src/main/java/com/student/demo/service -Force
mkdir worker-service/src/main/resources -Force

Write-Host "Moving core-libs files..."
Move-Item -Path "src/main/java/com/student/demo/entity/*" -Destination "core-libs/src/main/java/com/student/demo/entity" -Force
Move-Item -Path "src/main/java/com/student/demo/repository/*" -Destination "core-libs/src/main/java/com/student/demo/repository" -Force
Move-Item -Path "src/main/resources/db/migration/*" -Destination "core-libs/src/main/resources/db/migration" -Force

# Move specific configs to core-libs
Move-Item -Path "src/main/java/com/student/demo/config/RabbitMQConfig.java" -Destination "core-libs/src/main/java/com/student/demo/config" -Force
Move-Item -Path "src/main/java/com/student/demo/config/RedisPubSubConfig.java" -Destination "core-libs/src/main/java/com/student/demo/config" -Force

Write-Host "Moving api-service files..."
Move-Item -Path "src/main/java/com/student/demo/controller/*" -Destination "api-service/src/main/java/com/student/demo/controller" -Force
Move-Item -Path "src/main/java/com/student/demo/security/*" -Destination "api-service/src/main/java/com/student/demo/security" -Force

# Move API specific configs
Move-Item -Path "src/main/java/com/student/demo/config/JwtConfig.java" -Destination "api-service/src/main/java/com/student/demo/config" -Force
Move-Item -Path "src/main/java/com/student/demo/config/RateLimitConfig.java" -Destination "api-service/src/main/java/com/student/demo/config" -Force
Move-Item -Path "src/main/java/com/student/demo/config/SecurityConfig.java" -Destination "api-service/src/main/java/com/student/demo/config" -Force
Move-Item -Path "src/main/java/com/student/demo/config/WebMvcConfig.java" -Destination "api-service/src/main/java/com/student/demo/config" -Force

# Move services
Move-Item -Path "src/main/java/com/student/demo/service/CodeFileService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/DashboardService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/HistoryService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/ProjectService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/SseService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/TokenService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force
Move-Item -Path "src/main/java/com/student/demo/service/UserService.java" -Destination "api-service/src/main/java/com/student/demo/service" -Force

# Move properties
Copy-Item -Path "src/main/resources/application.properties" -Destination "api-service/src/main/resources/application.properties" -Force
Copy-Item -Path "src/main/resources/application.properties" -Destination "worker-service/src/main/resources/application.properties" -Force

Write-Host "Moving worker-service files..."
Move-Item -Path "src/main/java/com/student/demo/mq/*" -Destination "worker-service/src/main/java/com/student/demo/mq" -Force

# We leave CodeAnalyzerService.java alone for a second because we need to split it, we will use AI to recreate it
Write-Host "Migration script completed."

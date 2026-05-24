FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y docker.io && rm -rf /var/lib/apt/lists/*
COPY target/worker-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8089
CMD ["java", "-jar", "app.jar"]

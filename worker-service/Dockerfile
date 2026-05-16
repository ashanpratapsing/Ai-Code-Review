FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/worker-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8088
CMD ["java", "-jar", "app.jar"]

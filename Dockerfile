FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/aicodereview-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
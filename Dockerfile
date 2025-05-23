FROM openjdk:17-jdk-slim
WORKDIR /app
COPY projetc_backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dserver.port=$PORT", "-jar", "app.jar"]
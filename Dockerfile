FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Expect the Spring Boot fat jar to be built locally first:
# ./gradlew clean bootJar
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

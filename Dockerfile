# Use the official Eclipse Temurin image for Java 17
FROM eclipse-temurin:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean install -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/resume-analyzer-0.0.1-SNAPSHOT.jar"]
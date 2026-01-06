# Use the official Eclipse Temurin JDK 21 Alpine image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy backend files
COPY backend/test-spring/pom.xml .
COPY backend/test-spring/mvnw .
COPY backend/test-spring/.mvn .mvn

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/test-spring/src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create uploads directory
RUN mkdir -p uploads

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "target/test-spring-0.0.1-SNAPSHOT.jar"]
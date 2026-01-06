FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy backend files
COPY backend/test-spring/pom.xml .
COPY backend/test-spring/mvnw .
COPY backend/test-spring/.mvn .mvn

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/test-spring/src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create uploads directory
RUN mkdir -p uploads

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/test-spring-0.0.1-SNAPSHOT.jar"]
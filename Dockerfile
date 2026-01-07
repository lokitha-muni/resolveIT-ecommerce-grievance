# Multi-stage build: Backend + Frontend in one container

# Stage 1: Build Backend
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app
COPY backend/test-spring/pom.xml .
COPY backend/test-spring/mvnw .
COPY backend/test-spring/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY backend/test-spring/src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime with Backend + Frontend
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install nginx for frontend
RUN apk add --no-cache nginx

# Copy backend jar
COPY --from=backend-build /app/target/test-spring-0.0.1-SNAPSHOT.jar app.jar

# Copy frontend files
COPY frontend/ /usr/share/nginx/html/
COPY frontend/nginx.conf /etc/nginx/nginx.conf

# Create uploads directory
RUN mkdir -p uploads

# Create startup script
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'nginx &' >> /start.sh && \
    echo 'java -jar app.jar' >> /start.sh && \
    chmod +x /start.sh

# Expose ports
EXPOSE 8080 80

# Start both services
CMD ["/start.sh"]
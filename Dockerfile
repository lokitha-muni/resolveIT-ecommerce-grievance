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

# Install nginx and supervisor
RUN apk add --no-cache nginx supervisor

# Copy backend jar
COPY --from=backend-build /app/target/test-spring-0.0.1-SNAPSHOT.jar app.jar

# Copy frontend files to nginx directory
COPY frontend/ /usr/share/nginx/html/

# Create nginx config for frontend + backend proxy
RUN echo 'events { worker_connections 1024; }' > /etc/nginx/nginx.conf && \
    echo 'http {' >> /etc/nginx/nginx.conf && \
    echo '  include /etc/nginx/mime.types;' >> /etc/nginx/nginx.conf && \
    echo '  server {' >> /etc/nginx/nginx.conf && \
    echo '    listen 80;' >> /etc/nginx/nginx.conf && \
    echo '    root /usr/share/nginx/html;' >> /etc/nginx/nginx.conf && \
    echo '    index index.html;' >> /etc/nginx/nginx.conf && \
    echo '    location / { try_files $uri $uri/ /index.html; }' >> /etc/nginx/nginx.conf && \
    echo '    location /api/ {' >> /etc/nginx/nginx.conf && \
    echo '      proxy_pass http://localhost:8080;' >> /etc/nginx/nginx.conf && \
    echo '      proxy_set_header Host $host;' >> /etc/nginx/nginx.conf && \
    echo '    }' >> /etc/nginx/nginx.conf && \
    echo '  }' >> /etc/nginx/nginx.conf && \
    echo '}' >> /etc/nginx/nginx.conf

# Create supervisor config
RUN echo '[supervisord]' > /etc/supervisord.conf && \
    echo 'nodaemon=true' >> /etc/supervisord.conf && \
    echo '[program:backend]' >> /etc/supervisord.conf && \
    echo 'command=java -jar /app/app.jar' >> /etc/supervisord.conf && \
    echo 'autostart=true' >> /etc/supervisord.conf && \
    echo 'autorestart=true' >> /etc/supervisord.conf && \
    echo '[program:nginx]' >> /etc/supervisord.conf && \
    echo 'command=nginx -g "daemon off;"' >> /etc/supervisord.conf && \
    echo 'autostart=true' >> /etc/supervisord.conf && \
    echo 'autorestart=true' >> /etc/supervisord.conf

# Create uploads directory
RUN mkdir -p uploads

# Expose port 80 (nginx will proxy to backend on 8080)
EXPOSE 80

# Start both services with supervisor
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
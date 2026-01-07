FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY backend/test-spring/pom.xml .
COPY backend/test-spring/mvnw .
COPY backend/test-spring/.mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY backend/test-spring/src ./src

RUN ./mvnw clean package -DskipTests -B
RUN mkdir -p uploads

EXPOSE 8080

CMD ["java", "-jar", "target/test-spring-0.0.1-SNAPSHOT.jar"]
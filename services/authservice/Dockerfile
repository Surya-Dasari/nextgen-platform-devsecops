# -------- BUILD STAGE --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy only pom first (for caching dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Run as non-root user (best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

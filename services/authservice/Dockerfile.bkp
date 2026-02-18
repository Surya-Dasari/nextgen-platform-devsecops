# ---------- Stage 1: Build ----------
FROM registry.access.redhat.com/ubi9/openjdk-17 AS builder

USER root

WORKDIR /app

# Copy source
COPY services/authservice/pom.xml .
COPY services/authservice/src ./src

# Install maven and build
RUN microdnf install -y maven && \
    mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

USER root

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9001

USER 1001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


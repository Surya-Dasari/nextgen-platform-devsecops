# ---------- Stage 1: Build ----------
FROM registry.access.redhat.com/ubi9/openjdk-17 AS builder

USER root

WORKDIR /app

# Copy source
COPY services/userservice/pom.xml .
COPY services/userservice/src ./src

# Install maven
RUN microdnf install -y maven && \
    mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

USER root

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 9002
	
# OpenShift compatibility (allow arbitrary UID)
RUN chgrp -R 0 /app && \
    chmod -R g=u /app

USER 1001

# Default command
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


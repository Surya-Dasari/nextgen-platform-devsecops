# ---------- Stage 1 ----------
FROM registry.access.redhat.com/ubi9/openjdk-17 AS builder

USER root

WORKDIR /app

COPY services/apiservice/pom.xml .
COPY services/apiservice/src ./src

RUN microdnf install -y maven && \
    mvn clean package -DskipTests

# ---------- Stage 2 ----------
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

USER root

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9000

USER 1001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


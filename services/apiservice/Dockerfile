FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

RUN useradd -m appuser
USER appuser

EXPOSE 9000

ENTRYPOINT ["java","-jar","app.jar"]

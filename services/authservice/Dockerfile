FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

EXPOSE 9001

ENTRYPOINT ["java","-jar","app.jar"]

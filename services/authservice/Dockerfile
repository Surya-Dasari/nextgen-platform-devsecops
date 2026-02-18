FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

WORKDIR /deployments

COPY target/*.jar app.jar

RUN chgrp -R 0 /deployments && \
    chmod -R g=u /deployments

EXPOSE 9001

CMD ["java", "-jar", "app.jar"]


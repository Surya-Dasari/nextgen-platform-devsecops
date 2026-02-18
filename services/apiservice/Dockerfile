FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

USER root

WORKDIR /deployments

COPY target/*.jar app.jar

RUN chgrp -R 0 /deployments && \
    chmod -R g=u /deployments

USER 1001

EXPOSE 9001

CMD ["java", "-jar", "app.jar"]


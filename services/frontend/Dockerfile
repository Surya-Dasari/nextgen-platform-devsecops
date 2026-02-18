FROM registry.access.redhat.com/ubi9/nodejs-18:latest

USER root

WORKDIR /opt/app-root/src

COPY package*.json ./

RUN npm install --omit=dev

COPY . .

RUN chgrp -R 0 /opt/app-root/src && \
    chmod -R g=u /opt/app-root/src

USER 1001

EXPOSE 9080

CMD ["node", "server.js"]


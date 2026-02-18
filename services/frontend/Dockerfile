# ---------- Base Image ----------
FROM registry.access.redhat.com/ubi9/nodejs-18

WORKDIR /app

# Copy package files first
COPY services/frontend/package.json .

# Install dependencies
RUN npm install

# Copy application code
COPY services/frontend .

# Expose port
EXPOSE 9080

# OpenShift compatibility
USER 1001

# Start application
CMD ["node", "server.js"]


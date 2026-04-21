FROM node:18-alpine

WORKDIR /app

# Copy only package.json first (cache optimization)
COPY package.json package-lock.json* ./

RUN npm install --omit=dev

# Copy remaining files
COPY . .

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENV PORT=9080
EXPOSE 9080

CMD ["node", "server.js"]

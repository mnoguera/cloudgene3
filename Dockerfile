# Multi-stage Dockerfile for Cloudgene 3
# Stage 1: Build stage with Maven and Node.js
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /build

# Install required build tools (Node.js is managed by frontend-maven-plugin)
RUN apt-get update && \
    apt-get install -y unzip && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
COPY aot-jar.properties .

# Copy source code
COPY src ./src

# Build the application
# This will:
# 1. Install Node.js and npm via frontend-maven-plugin
# 2. Build the webapp frontend
# 3. Compile Java code
# 4. Create the assembly package
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with minimal JRE
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/webapp /app/logs /app/tmp-files /app/data /mnt/azure/logs

# Copy the built artifact from builder stage
# The assembly plugin creates a zip file, we need to extract it
COPY --from=builder /build/target/cloudgene-*.zip /tmp/cloudgene.zip

# Extract the application
RUN unzip /tmp/cloudgene.zip -d /tmp && \
    mv /tmp/cloudgene-*/* /app/ && \
    rm -rf /tmp/cloudgene.zip /tmp/cloudgene-* && \
    chmod +x /app/cloudgene

# Set build-time argument for JWT secret (no default; must be provided)
ARG JWT_GENERATOR_SIGNATURE_SECRET

# Set environment variables
ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/app:${PATH}" \
    JWT_GENERATOR_SIGNATURE_SECRET=${JWT_GENERATOR_SIGNATURE_SECRET}

# Expose the default port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/v2/server/version || exit 1

# Run the application
ENTRYPOINT ["./cloudgene"]
CMD ["server"]

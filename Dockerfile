# Build stage - use Eclipse Temurin 21 JDK Alpine
FROM eclipse-temurin:21-jdk-alpine AS build

# Install necessary packages for better network handling
RUN apk add --no-cache curl wget

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files first for better caching
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (better Docker layer caching)
RUN ./gradlew --no-daemon dependencies || true

# Copy source code and configuration
COPY src ./src
COPY config ./config

# Build the application with extended timeouts and no daemon
ENV JAVA_TOOL_OPTIONS "-Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1"
RUN ./gradlew --no-daemon --stacktrace \
    -Dorg.gradle.daemon=false \
    -Dorg.gradle.parallel=false \
    -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m" \
    build -x test

# Runtime stage - use Eclipse Temurin 21 JRE Alpine for smaller final image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Create non-root user for security (Alpine way)
RUN addgroup -g 1001 -S appuser && \
    adduser -S -D -H -u 1001 -h /app -s /sbin/nologin -G appuser appuser

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appuser /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/messages/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

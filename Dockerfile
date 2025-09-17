# Use OpenJDK 17 Alpine as base image for smaller size
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew build -x test

# Create app directory for the jar
RUN mkdir -p /app/build/libs

# Find and copy the built jar file
RUN cp build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Install curl for health check
RUN apk add --no-cache curl

# Create non-root user for security (Alpine way)
RUN addgroup -g 1001 -S appuser && \
    adduser -S -D -H -u 1001 -h /app -s /sbin/nologin -G appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/messages/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
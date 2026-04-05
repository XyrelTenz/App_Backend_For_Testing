# =============================================================================
# Multi-stage Dockerfile for Kotlin/Spring Boot 3 Driving App
# Stage 1: Build with Gradle
# Stage 2: Minimal JRE runtime image
# =============================================================================

# ── Stage 1: Builder ──────────────────────────────────────────────────────────
FROM gradle:8.10-jdk21-alpine AS builder

LABEL maintainer="xyrel"
LABEL description="Driving App Backend - Kotlin/Spring Boot 3"

WORKDIR /app

# Copy only dependency files first (layer caching)
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle/

# Pre-fetch dependencies (cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy full source
COPY src src/

# Build the JAR (skip tests for production image)
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

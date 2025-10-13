# Multi-stage build for Spring Boot application
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .

# Force Linux platform for dependencies
ENV MAVEN_OPTS="-Dos.detected.classifier=linux-x86_64"

# Skip the go-offline step and build directly
COPY src ./src
RUN mvn clean package -DskipTests -Dos.detected.classifier=linux-x86_64

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check using our custom endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health-check || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

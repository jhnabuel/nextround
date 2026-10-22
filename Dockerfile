FROM ubuntu:latest
LABEL authors="johnabuel"

ENTRYPOINT ["top", "-b"]

# ==========================================
# 1. Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and dependencies first for aggressive layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build executable jar (skipping tests)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Find the fat JAR specifically (ignoring *.jar.original) and standardize its name
RUN mv target/*.jar app.jar && rm -f target/*.jar.original 2>/dev/null || true

# ==========================================
# 2. Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create an unprivileged non-root user and group for security
RUN addgroup -S nextround && adduser -S nextround -G nextround

# Copy only the compiled fat JAR from the builder stage
COPY --from=builder --chown=nextround:nextround /app/app.jar app.jar

USER nextround

# Container-aware memory allocation (limits JVM heap to 75% of available container RAM)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
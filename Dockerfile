
# ==========================================
# 1. Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests
RUN mv target/*.jar app.jar

# ==========================================
# 2. Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S nextround && adduser -S nextround -G nextround

COPY --from=builder --chown=nextround:nextround /app/app.jar app.jar

USER nextround

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=65.0"
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

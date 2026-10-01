# ==============================================================================
# Stage 1: Build the Spring Boot application using Maven and Temurin JDK 21
# ==============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Grant executable permission to Maven wrapper
RUN chmod +x mvnw

# Copy source code and package application JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Stage 2: Minimal, secure production runtime using Temurin JRE 21
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

# Create non-root system user for security hardening
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy packaged JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set file ownership to non-root user
RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

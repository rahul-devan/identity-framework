# Use Eclipse Temurin JDK 21 as base image
FROM eclipse-temurin:21-jdk-alpine as build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health check (must be done as root)
RUN apk add --no-cache curl

# Create non-root user for security (Alpine uses addgroup/adduser)
RUN addgroup -g 1001 spring && adduser -D -u 1001 -G spring spring

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for H2 database files and logs
RUN mkdir -p /app/data /app/logs && chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the default Spring Boot port
EXPOSE 8080

# Health check

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
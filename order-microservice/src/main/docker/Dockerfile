## Build stage
FROM gradle:8.5-jdk21 AS build

# Set working directory
WORKDIR /workspace

COPY . /workspace/

# Build the inventory-microservice
RUN gradle :order-microservice:clean :order-microservice:build -x test --no-daemon

## Runtime stage
FROM eclipse-temurin:21-jre AS order-microservice

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /workspace/order-microservice/build/libs/order-microservice.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx2048m"

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app

# Run as non-root user
USER appuser

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
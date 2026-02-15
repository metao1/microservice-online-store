## Build stage
FROM gradle:9-jdk21 AS build

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

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    usermod -a -G 1000 appuser && \
    chown -R appuser:appuser /app

# Run as non-root user
USER appuser

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
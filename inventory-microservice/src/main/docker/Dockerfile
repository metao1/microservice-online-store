## Build stage
FROM gradle:9-jdk21 AS build

# Set working directory
WORKDIR /workspace

COPY . /workspace/

# Build the inventory-microservice
RUN gradle :inventory-microservice:clean :inventory-microservice:build -x test --no-daemon

## Runtime stage
FROM gcr.io/distroless/java21-debian12:nonroot

COPY --from=build /workspace/inventory-microservice/build/libs/inventory-microservice.jar app.jar

WORKDIR /app

EXPOSE 8083

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
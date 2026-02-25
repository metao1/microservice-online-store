# syntax=docker/dockerfile:1.6
## Build stage
FROM gradle:9-jdk21 AS build

WORKDIR /workspace

# Copy Gradle wrapper and root build files (preserve structure)
COPY gradle /workspace/gradle
COPY gradlew settings.gradle build.gradle /workspace/
# Copy shared libs and this service sources
COPY shared-kernel /workspace/shared-kernel
COPY shared-test /workspace/shared-test
COPY kafka-lib /workspace/kafka-lib
COPY payment-microservice /workspace/payment-microservice

# Build layered fat jar (skip tests; they should run in CI)
RUN ./gradlew :payment-microservice:bootJar -x test --no-daemon --configuration-cache
# Extract layers for slimmer runtime image
RUN java -Djarmode=layertools -jar payment-microservice/build/libs/payment-microservice.jar extract --destination /workspace/layers

## Runtime stage
FROM gcr.io/distroless/java21-debian12 AS payment-microservice

# Set working directory
WORKDIR /app

# Copy layers separately to leverage Docker cache
COPY --from=build /workspace/layers/dependencies/ /workspace/layers/snapshot-dependencies/ /workspace/layers/spring-boot-loader/ /workspace/layers/application/ ./

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]

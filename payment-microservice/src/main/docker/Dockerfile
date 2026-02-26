# syntax=docker/dockerfile:1.6
## Build stage
FROM gradle:9-jdk21 AS build

WORKDIR /workspace

# Copy Gradle root files and required modules only
COPY settings.gradle build.gradle gradle.properties /workspace/
COPY shared-kernel/build.gradle /workspace/shared-kernel/build.gradle
COPY shared-kernel/src/main /workspace/shared-kernel/src/main
COPY kafka-lib/build.gradle /workspace/kafka-lib/build.gradle
COPY kafka-lib/src/main /workspace/kafka-lib/src/main
COPY payment-microservice/build.gradle /workspace/payment-microservice/build.gradle
COPY payment-microservice/src/main /workspace/payment-microservice/src/main

# Build layered fat jar (skip tests; they should run in CI)
RUN --mount=type=cache,id=gradle-user-home,target=/home/gradle/.gradle \
    --mount=type=cache,id=gradle-project-cache,target=/workspace/.gradle \
    gradle :payment-microservice:bootJar -x test --no-daemon --configuration-cache --build-cache --parallel
# Extract layers for slimmer runtime image
RUN java -Djarmode=layertools -jar payment-microservice/build/libs/payment-microservice.jar extract --destination /workspace/layers

## Runtime stage
FROM gcr.io/distroless/java21-debian12 AS payment-microservice

# Set working directory
WORKDIR /app

# Copy layers separately to leverage Docker cache
COPY --from=build /workspace/layers/dependencies/ /workspace/layers/snapshot-dependencies/ /workspace/layers/spring-boot-loader/ /workspace/layers/application/ ./

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]

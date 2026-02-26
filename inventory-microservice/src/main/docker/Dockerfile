# syntax=docker/dockerfile:1.6
## Build stage
FROM gradle:9-jdk21 AS build

WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties /workspace/
COPY shared-kernel/build.gradle /workspace/shared-kernel/build.gradle
COPY shared-kernel/src/main /workspace/shared-kernel/src/main
COPY kafka-lib/build.gradle /workspace/kafka-lib/build.gradle
COPY kafka-lib/src/main /workspace/kafka-lib/src/main
COPY inventory-microservice/build.gradle /workspace/inventory-microservice/build.gradle
COPY inventory-microservice/src/main /workspace/inventory-microservice/src/main

# Build layered fat jar (skip tests; they should run in CI)
RUN --mount=type=cache,id=gradle-user-home,target=/home/gradle/.gradle \
    --mount=type=cache,id=gradle-project-cache,target=/workspace/.gradle \
    gradle :inventory-microservice:bootJar -x test --no-daemon --configuration-cache --build-cache --parallel

# Extract layers for slimmer runtime image
RUN java -Djarmode=layertools -jar inventory-microservice/build/libs/inventory-microservice.jar extract --destination /workspace/layers

## Runtime stage
FROM gcr.io/distroless/java21-debian12 AS inventory-microservice

WORKDIR /app

# Copy layers separately to leverage Docker cache
COPY --from=build /workspace/layers/dependencies/ /workspace/layers/snapshot-dependencies/ /workspace/layers/spring-boot-loader/ /workspace/layers/application/ ./

EXPOSE 8083

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]

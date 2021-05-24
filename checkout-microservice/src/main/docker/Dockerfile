FROM adoptopenjdk:11-jdk-hotspot as builder
LABEL stage=builder

RUN    addgroup checkout \
    && apt-get update \
    && apt-get install build-essential -y \
    && apt-get install dos2unix -y \
    && adduser --system --disabled-password --disabled-login checkout \
    && adduser checkout checkout \
    && mkdir -p /opt/checkout \
    && mkdir -p /var/log/checkout \
    && mkdir -p /code \
    && chown -R checkout:checkout /opt/checkout \
    && chown -R checkout:checkout /code

COPY . /code/

RUN    cd /code/ \
    && ls -ls \
    && dos2unix gradlew \
    && chmod +x gradlew \
    && rm -rf build \
    && sleep 1 \
    && ./gradlew clean build bootJar -x test \
    && cp build/libs/*.jar /opt/checkout/checkout.jar

FROM openjdk:11.0.6-jre-slim-buster

COPY --from=builder /opt/checkout/checkout.jar /usr/bin/checkout.jar

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Xms256m -Xmx2048m" \
    SLEEP=1 \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

CMD echo "The checkout microservice will start in ${SLEEP}s..." && \
    sleep ${SLEEP} && \
    java \
        ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom \
            -jar /usr/bin/checkout.jar


FROM adoptopenjdk:11-jdk-hotspot as builder
LABEL stage=builder

ADD . /code/

RUN    addgroup checkout \
    && apt-get update \
    && apt-get install build-essential -y \
    && apt-get install dos2unix -y \
    && adduser --system --disabled-password --disabled-login checkout \
    && adduser sentinel sentinel \
    && mkdir -p /opt/topclu \
    && mkdir -p /code \
    && chown -R checkout:checkout /opt/checkout \
    && chown -R checkout:checkout /code

RUN    cd /code/ \
    && dos2unix gradlew \
    && mkdir -p /var/log/checkout \
    && mkdir -p /home/checkout \
    && mkdir -p /opt/checkout \
    && chmod +x gradlew

RUN cd /code/ \
    && rm -rf build \
    && sleep 1 \
    && ./gradlew clean build  bootJar \
    && cp build/resources/main/redisson.yaml /home/sentinel/redisson.yaml \
    && cp build/libs/*.jar /opt/checkout/checkout.jar

FROM openjdk:11.0.6-jre-slim-buster

COPY --from=builder /opt/checkout/sentinel.jar /usr/bin/checkout.jar

COPY --from=builder /home/checkout/redisson.yaml /home/checkout/redisson.yaml

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Xms256m -Xmx2048m" \
    SLEEP=1 \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

CMD echo "The checkout microservice will start in ${SLEEP}s..." && \
    sleep ${SLEEP} && \
    java \
        ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom \
            -jar /usr/bin/redisson.jar


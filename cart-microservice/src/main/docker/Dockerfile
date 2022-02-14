FROM openjdk:16-slim as builder

WORKDIR code

RUN    apt-get --allow-unauthenticated update \
    && apt-get install build-essential -y \
    && mkdir -p /opt/checkout \
    && mkdir -p /var/log/checkout

COPY ../../../ ./

RUN chmod +x gradlew

RUN rm -rf build \
    && sleep 1 \
    && ./gradlew clean build \
    && cp build/libs/*-SNAPSHOT.jar /opt/checkout/checkout.jar

FROM openjdk:16-slim as runner

COPY --from=builder /opt/checkout/checkout.jar /usr/bin/checkout.jar

#USER checkout

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


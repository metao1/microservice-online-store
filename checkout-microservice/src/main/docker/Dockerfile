FROM gradle:7.3.0-jdk17-alpine as gradle

WORKDIR /code

COPY . .

RUN gradle --build-cache compileJava
RUN gradle --build-cache assemble

FROM gradle as checkout-microservice

WORKDIR /application

COPY --from=gradle /code/build/libs/*-SNAPSHOT.jar /application/app.jar

ENV JAVA_OPTS="-Xms256m -Xmx2048m" \
    SLEEP=1

USER 1001

CMD echo "RACIM will start in ${SLEEP}s..." && \
    sleep ${SLEEP} && \
    java \
        ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom \
        -jar /application/app.jar

FROM eclipse-temurin:21 AS jdk

COPY --chown=user:user build/libs/inventory-microservice.jar app.jar

FROM jdk AS inventory-microservice

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx2048m -XX:UseSVE=0" \
    SLEEP=1

# Run as non-root user
USER 1001

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
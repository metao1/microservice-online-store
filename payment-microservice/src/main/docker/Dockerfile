FROM eclipse-temurin:21 AS jdk

# Assuming the JAR file will be named payment-microservice.jar
# Spring Boot Gradle plugin usually names it after the project/module name.
COPY --chown=user:user build/libs/payment-microservice.jar app.jar

FROM jdk AS payment-microservice

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx1024m" # Adjusted memory for a potentially smaller service

# Run as non-root user
USER 1001

# Expose the port the application will run on
EXPOSE 8084

# Start the application
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]

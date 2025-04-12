package com.metao.shared.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
public class BaseKafkaIT {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.7.0")
            .asCompatibleSubstituteFor("apache/kafka"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        if (!kafkaContainer.isRunning()) {
            kafkaContainer.start();
            kafkaContainer.waitingFor(Wait.forLogMessage(".*Kafka server started.*", 1));
            log.info("Kafka container is started.");
        }
    }

}
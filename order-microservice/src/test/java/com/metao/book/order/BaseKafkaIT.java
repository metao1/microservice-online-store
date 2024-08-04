package com.metao.book.order;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class BaseKafkaIT {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
        .withEmbeddedZookeeper();

    @BeforeAll
    public void setup() {
        kafkaContainer.start();
        kafkaContainer.waitingFor(Wait.forLogMessage(".*Kafka server started.*", 1));
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        if (!kafkaContainer.isRunning()) {
            kafkaContainer.start();
        }
    }

}
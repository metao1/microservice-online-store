package com.metao.book.order.utils;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class BaseRedpandaIntegrationTest {

    private static final RedpandaContainer container = new RedpandaContainer(
        "docker.redpanda.com/vectorized/redpanda:v22.3.13");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", container::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", container::getSchemaRegistryAddress);
        if (!container.isRunning()) {
            container.start();
        }
    }

}
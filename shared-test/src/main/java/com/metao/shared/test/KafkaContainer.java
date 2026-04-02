package com.metao.shared.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainer {

    static final org.testcontainers.kafka.KafkaContainer kafka = new org.testcontainers.kafka.KafkaContainer(
        DockerImageName.parse("apache/kafka:3.8.0")
    ).withReuse(true); // enable container reuse to speed up repeated test runs

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}

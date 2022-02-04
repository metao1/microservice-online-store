package com.metao.product.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.SocketUtils;

import java.time.Duration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "management.server.port=${test.port}")
class ApiGatewayApplicationTests {

    private static int managementPort;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void beforeAllSetup() {
        managementPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("test.port", String.valueOf(managementPort));
        String baseUrl = "http://localhost:" + 8081;
        webTestClient = WebTestClient.bindToServer().responseTimeout(Duration.ofSeconds(10)).baseUrl(baseUrl).build();
    }

    @AfterAll
    public static void afterAllSetup() {
        System.clearProperty("test.port");
    }

    @Test
    public void actuatorManagementPort() {
        webTestClient.get()
                .uri("http://localhost:" + managementPort + "/actuator/gateway/routes")
                .exchange().expectStatus().isOk();
    }

}

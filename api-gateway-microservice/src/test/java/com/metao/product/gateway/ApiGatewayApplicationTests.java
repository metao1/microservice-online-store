package com.metao.product.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.SocketUtils;

import java.time.Duration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = {ApiGatewayApplicationTests.TestConfig.class},
        webEnvironment = RANDOM_PORT, properties = "management.server.port=${test.port}")
class ApiGatewayApplicationTests {

    private static int managementPort;

    @LocalServerPort
    private static int localPort;

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
    public void contextLoad() {
        webTestClient.get().uri("/products/?limit=12&offset=0").exchange().expectStatus().isOk();
    }

    @Test
    public void actuatorManagementPort() {
        webTestClient.get()
                .uri("http://localhost:" + managementPort + "/actuator/gateway/routes")
                .exchange().expectStatus().isOk();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @LoadBalancerClient(name = "httpbin", configuration = LoadBalancerConfig.class)
    protected static class TestConfig {

        @Bean
        public HttpBinCompatibleController httpBinCompatibleController() {
            return new HttpBinCompatibleController();
        }
    }

    protected static class LoadBalancerConfig {
        @LocalServerPort
        private int serverPort;

        @Bean
        public ServiceInstanceListSupplier fixedServiceInstanceListSupplier(Environment env) {
            return ServiceInstanceListSupplier.fixed(env).instance("localhost", serverPort, "httpbin")
                    .build();
        }
    }
}

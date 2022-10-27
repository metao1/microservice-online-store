package com.metao.book.cart.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasePostgresIntegrationTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13.1")
        .withUsername("testUsername")
        .withPassword("testPassword")
        .withDatabaseName("testDatabase");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("db_url", postgres::getJdbcUrl);
        registry.add("db_username", postgres::getUsername);
        registry.add("db_password", postgres::getPassword);
    }

    @BeforeAll
    public static void setupRedisClient() {
        postgres.start();
    }

    @AfterAll
    public static void destroy() {
        postgres.stop();
    }

}
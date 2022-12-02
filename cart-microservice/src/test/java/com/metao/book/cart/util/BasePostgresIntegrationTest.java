package com.metao.book.cart.util;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
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
        postgres.start();
    }

}
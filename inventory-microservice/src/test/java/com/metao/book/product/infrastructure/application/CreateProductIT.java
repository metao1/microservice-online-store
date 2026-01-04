package com.metao.book.product.infrastructure.application;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateProductIT extends KafkaContainer {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        var requestBody = """
            {
                "sku": "SKU1234567",
                "title": "Test Product",
                "description": "Test Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .header("Location", endsWith("/products/SKU1234567"));
    }
}

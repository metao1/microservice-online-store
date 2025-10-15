package com.metao.book.payment.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerIT extends KafkaContainer {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        // Given
        var requestBody = """
        {
            "orderId": "order-123",
            "amount": 100.00,
            "currency": "USD",
            "paymentMethodType": "PAYPAL",
            "paymentMethodDetails": "****-5678"
        }
        """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/payments")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("paymentId", notNullValue())
            .body("orderId", equalTo("order-123"))
            .body("amount", equalTo(100.00f))
            .body("currency", equalTo("USD"))
            .body("status", equalTo("PENDING"));
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given - First create a payment
        var requestBody = """
        {
            "orderId": "order-456",
            "amount": 100.00,
            "currency": "USD",
            "paymentMethodType": "PAYPAL",
            "paymentMethodDetails": "****-5678"
        }
        """;

        String paymentId = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/payments")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("orderId", equalTo("order-456"))
            .extract()
            .path("paymentId");

        // When & Then - Process the payment
        String processedStatus = given()
            .contentType(ContentType.JSON)
        .when()
            .post("/payments/{paymentId}/process", paymentId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("paymentId", equalTo(paymentId))
            .body("status", anyOf(equalTo("SUCCESSFUL"), equalTo("FAILED")))
            .extract()
            .path("status");

        // Verify the payment status was updated (should be either SUCCESSFUL or FAILED)
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/payments/{paymentId}", paymentId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("paymentId", equalTo(paymentId))
            .body("status", equalTo(processedStatus));
    }
}
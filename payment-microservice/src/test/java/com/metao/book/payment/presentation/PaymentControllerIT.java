package com.metao.book.payment.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;

import com.metao.book.payment.domain.model.valueobject.PaymentMethod;

import com.metao.shared.test.BaseKafkaTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerIT extends BaseKafkaTest {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/payments";
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        // Given
        Map<String, Object> createPaymentRequest = new HashMap<>();
        createPaymentRequest.put("orderId", "order-123");
        createPaymentRequest.put("amount", BigDecimal.valueOf(100.00));
        createPaymentRequest.put("currency", Currency.getInstance("USD"));
        createPaymentRequest.put("paymentMethodType", PaymentMethod.Type.CREDIT_CARD);
        createPaymentRequest.put("paymentMethodDetails", "****-1234");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(createPaymentRequest)
            .when()
            .post()
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
        Map<String, Object> createPaymentRequest = new HashMap<>();
        createPaymentRequest.put("orderId", "order-456");
        createPaymentRequest.put("amount", BigDecimal.valueOf(200.00));
        createPaymentRequest.put("currency", Currency.getInstance("USD"));
        createPaymentRequest.put("paymentMethodType", PaymentMethod.Type.CREDIT_CARD);
        createPaymentRequest.put("paymentMethodDetails", "****-5678");

        String paymentId = given()
            .contentType(ContentType.JSON)
            .body(createPaymentRequest)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("paymentId");

        // When & Then - Process the payment
        String processedStatus = given()
            .contentType(ContentType.JSON)
        .when()
            .post("/{paymentId}/process", paymentId)
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
            .get("/{paymentId}", paymentId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("paymentId", equalTo(paymentId))
            .body("status", equalTo(processedStatus));
    }
}
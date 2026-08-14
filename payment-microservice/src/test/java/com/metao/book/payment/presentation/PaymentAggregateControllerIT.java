package com.metao.book.payment.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.shared.test.KafkaContainerBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentAggregateControllerIT extends KafkaContainerBase {

    private static final String USER_TOKEN = "mock-payment-jwt-token";

    @Autowired
    private PaymentRepository paymentRepository;

    @LocalServerPort
    private Integer port;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        org.mockito.Mockito.when(jwtDecoder.decode(USER_TOKEN)).thenReturn(
            Jwt.withTokenValue(USER_TOKEN)
                .header("alg", "none")
                .subject("payment-test-user")
                .audience(List.of("account"))
                .claim("roles", List.of("CUSTOMER", "ADMIN"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        );
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        // Given
        var requestBody = """
            {
                "orderId": "order-123",
                "amount": 100.00,
                "currency": "EUR",
                "paymentMethodType": "PAYPAL",
                "paymentMethodDetails": "****-5678"
            }
            """;

        // When & Then
        given()
            .auth().oauth2(USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/payments")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("paymentId", notNullValue())
            .body("orderId", equalTo("order-123"))
            .body("amount", equalTo(100.00f))
            .body("currency", equalTo("EUR"))
            .body("status", equalTo("PENDING"));
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given - First create a payment
        var requestBody = """
            {
                "orderId": "order-456",
                "amount": 100.00,
                "currency": "EUR",
                "paymentMethodType": "PAYPAL",
                "paymentMethodDetails": "****-5678"
            }
            """;

        var createdPaymentResponse = given()
            .auth().oauth2(USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/payments")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("orderId", equalTo("order-456"))
            .extract()
            .response();

        String paymentId = createdPaymentResponse.jsonPath().getString("paymentId");
        assertNotNull(paymentId);

        // When & Then - Process the payment
        var processedPaymentResponse = given()
            .auth().oauth2(USER_TOKEN)
            .contentType(ContentType.JSON)
            .when()
            .post("/payments/{paymentId}/process", paymentId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("paymentId", equalTo(paymentId))
            .body("status", anyOf(equalTo("SUCCESSFUL"), equalTo("FAILED")))
            .extract()
            .response();

        String processedStatus = processedPaymentResponse.jsonPath().getString("status");
        assertNotNull(processedStatus);

        var persistedPayment = paymentRepository.findById(PaymentId.of(paymentId)).orElseThrow();
        assertEquals(processedStatus, persistedPayment.getStatus().name());
    }
}

package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.service.ShoppingCartService;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.application.port.OrderRepository;
import com.metao.book.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.shared.test.KafkaContainerBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderManagementControllerIT extends KafkaContainerBase {

    private static final String USER_ID = "user123";
    private static final String USER_TOKEN = "mock-jwt-token-user";
    private static final String SKU = "SKU_E2E_001";
    private static final String PRODUCT_TITLE = "product123";
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(12.99);
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @LocalServerPort
    private Integer port;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        Mockito.when(jwtDecoder.decode(USER_TOKEN)).thenReturn(
            Jwt.withTokenValue(USER_TOKEN)
                .header("alg", "none")
                .subject(USER_ID)
                .audience(List.of("account"))
                .claim("roles", List.of("CUSTOMER", "ADMIN"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        );
        springDataOrderRepository.deleteAll();
        shoppingCartService.clearCart(USER_ID);
    }

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() {
            shoppingCartService.addItemToCart(
                USER_ID,
                    List.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY))
            );

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .post("/api/order")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("value", matchesPattern("[a-f0-9\\-]+"));
        }
    }

    @Nested
    @DisplayName("Order Status")
    class OrderStatusManagement {

        @Test
        @DisplayName("Should update status of an order successfully")
        void shouldUpdateStatusOfAnOrderSuccessfully() {
            var orderId = new OrderId("order123");
            orderRepository.save(new OrderAggregate(orderId, UserId.of(USER_ID)));

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .body(new UpdateStatusRequestDto("PAID"))
                .patch("/api/order/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .body(new UpdateStatusRequestDto("CANCELLED"))
                .patch("/api/order/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());
        }
    }

    @Nested
    @DisplayName("Order Queries")
    class OrderQueries {

        @Test
        @DisplayName("Should get customer orders successfully")
        void shouldGetCustomerOrdersSuccessfully() {
            orderRepository.save(new OrderAggregate(new OrderId("order123"), UserId.of(USER_ID)));

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .get("/api/order/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1));
        }

        @Test
        @DisplayName("Should get paged customer orders successfully")
        void shouldGetPagedCustomerOrdersSuccessfully() {
            orderRepository.save(new OrderAggregate(new OrderId("order123"), UserId.of(USER_ID)));
            orderRepository.save(new OrderAggregate(new OrderId("order124"), UserId.of(USER_ID)));

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + USER_TOKEN)
                .queryParam("offset", 0)
                .queryParam("limit", 1)
                .get("/api/order/me/paged")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("items", hasSize(1))
                .body("offset", equalTo(0))
                .body("limit", equalTo(1))
                .body("total", equalTo(2))
                .body("hasNext", equalTo(true))
                .body("hasPrevious", equalTo(false));
        }
    }

    @Nested
    @DisplayName("Security")
    class SecurityTests {

        @Test
        @DisplayName("Should return unauthorized when no auth token provided for order creation")
        void shouldReturnUnauthorizedWithoutAuth() {
            given()
                .contentType(ContentType.JSON)
                .post("/api/order")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should return unauthorized when no auth token provided for order queries")
        void shouldReturnUnauthorizedForQueriesWithoutAuth() {
            given()
                .contentType(ContentType.JSON)
                .get("/api/order/me")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}

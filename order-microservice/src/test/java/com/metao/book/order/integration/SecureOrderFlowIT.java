package com.metao.book.order.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.service.ShoppingCartService;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.shared.test.KafkaContainerBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Secure Order Flow Integration Tests")
class SecureOrderFlowIT extends KafkaContainerBase {

    private static final String USER_ID = "user_secure_123";
    private static final String USER_TOKEN = "mock-jwt-token-secure-user";
    private static final String SKU = "SKU_SECURE_001";
    private static final String PRODUCT_TITLE = "SecureProduct";
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(29.99);
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
        springDataOrderRepository.deleteAll();
        shoppingCartService.clearCart(USER_ID);
    }

    @Test
    @DisplayName("Complete secure order flow: add to cart, create order, verify authentication")
    void completeSecureOrderFlow() {
        // Add item to cart
        shoppingCartService.addItemToCart(
            USER_ID,
            List.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY))
        );

        // Create order with authentication
        String orderId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .body(new CreateOrderRequestDTO(USER_ID))
            .post("/api/order")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("value", matchesPattern("[a-f0-9\\-]+"))
            .extract()
            .path("value");

        assertThat(orderId).isNotNull();

        // Verify order can be retrieved with authentication
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .get("/api/order/customer")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(1));
    }

    @Test
    @DisplayName("Order creation without auth should fail")
    void orderCreationWithoutAuth_shouldFail() {
        shoppingCartService.addItemToCart(
            USER_ID,
            List.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY))
        );

        given()
            .contentType(ContentType.JSON)
            .body(new CreateOrderRequestDTO(USER_ID))
            .post("/api/order")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());

        // Verify no order was created
        var orders = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .get("/api/order/customer")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("$");
        
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("Cart operations require authentication")
    void cartOperations_requireAuthentication() {
        // GET cart without auth
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());

        // POST to cart without auth
        given()
            .contentType(ContentType.JSON)
            .body(List.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY)))
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());

        // DELETE from cart without auth
        given()
            .contentType(ContentType.JSON)
            .when()
            .delete("/cart/items/{sku}", SKU)
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Cart operations work with valid authentication")
    void cartOperations_withValidAuth_shouldSucceed() {
        // Add item to cart
        var item = List.of(new AddItemRequestDto(USER_ID,
            List.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY))));
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .body(item)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        // Get cart
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("shopping_cart_items", hasSize(1));

        // Clear cart
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .when()
            .delete("/cart")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify cart is empty
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("shopping_cart_items", hasSize(0));
    }

    @Test
    @DisplayName("Customer cannot access another customer's orders")
    void customer_isolation_orders() {
        // Save order for different user
        String otherUserId = "other_user_456";
        orderRepository.save(new OrderAggregate(new OrderId("order-other"), UserId.of(otherUserId)));

        // Current user should not see other user's order
        var orders = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + USER_TOKEN)
            .get("/api/order/customer")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("$");
        
        assertThat(orders).isEmpty();
    }
}

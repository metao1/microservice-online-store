package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderManagementControllerIT extends KafkaContainer {

    private static final String USER_ID = "user123";
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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
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
                Set.of(new ShoppingCartItem(SKU, PRODUCT_TITLE, BigDecimal.ONE, UNIT_PRICE, CURRENCY))
            );

            given()
                .contentType(ContentType.JSON)
                .body(new CreateOrderRequestDTO(USER_ID))
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
                .body(new UpdateStatusRequestDto("PAID"))
                .patch("/api/order/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

            given()
                .contentType(ContentType.JSON)
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
                .get("/api/order/customer/{userId}", USER_ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1));
        }
    }
}

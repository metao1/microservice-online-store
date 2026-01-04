package com.metao.book.order;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.protobuf.Timestamp;
import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.repository.JpaOrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@Slf4j
@DirtiesContext
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({@ConfigureWireMock(port = 8083, name = "inventory-microservice")})
class E2EProductPurchaseContainerIT extends KafkaContainer {

    private static final BigDecimal ONE = BigDecimal.ONE;

    private final String userId = "e2eUser";
    private final String sku1 = "SKU_E2E_001"; // Assume this product exists in inventory-microservice
    private final BigDecimal price1 = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("EUR");

    @LocalServerPort // Port for order-microservice
    private Integer orderMicroservicePort;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JpaOrderRepository jpaOrderRepository; // For test cleanup

    @Autowired
    private KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate;

    @Autowired
    private KafkaEventHandler kafkaEventHandler;

    @BeforeEach
    void setUp() {
        // Clean order-microservice DB before each test method execution in this ordered test class
        jpaOrderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        RestAssured.port = orderMicroservicePort;
    }

    @Test
    @Order(1)
    void step1_addProductToCart() {
        // Preliminary check that inventory-microservice has the product (optional)
        log.info("🔍 Checking inventory service for product: " + sku1);

        // Stub the inventory service check for step1
        stubFor(get(urlEqualTo("/products/" + sku1))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "sku": "%s",
                      "title": "Test Product",
                      "description": "Test product description",
                      "imageUrl": "https://example.com/image.jpg",
                      "price": 12.99,
                      "currency": "EUR",
                      "volume": 1.0,
                      "categories": ["test-category"],
                      "createdTime": "2025-01-01T00:00:00Z",
                      "updatedTime": "2025-01-01T00:00:00Z",
                      "inStock": true
                    }
                    """.formatted(sku1))));

        log.info("✅ Inventory service check passed for product: " + sku1);

        var addItemDTO = new AddItemRequestDto(
            userId,
            Set.of(
                new ShoppingCartItem(
                    sku1,
                    ONE,
                    price1,
                    currency
                )
            )
        );

        given()
            .contentType(ContentType.JSON)
            .body(addItemDTO)
            .when()
            .post("/cart")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        assertThat(shoppingCartRepository.findByUserIdAndSku(userId, sku1)).isPresent();
    }

    @Test
    @Order(2)
    void step2_viewCart() {
        // Ensure an item from step 1 is in the cart for this ordered test
        // If step1 failed or tests were run out of order, this might fail without this setup.
        if (shoppingCartRepository.findByUserIdAndSku(userId, sku1).isEmpty()) {
            shoppingCartRepository.save(new ShoppingCart(userId, sku1, price1, price1, ONE, currency));
        }

        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/cart/{userId}", userId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo(userId))
            .body("shopping_cart_items", hasSize(1))
            .body("shopping_cart_items[0].sku", equalTo(sku1));
    }

    @Test
    @Order(3)
    void step3_checkoutAndVerifyOrder() {
        // Ensure item from step 1 is in cart
        if (shoppingCartRepository.findByUserIdAndSku(userId, sku1).isEmpty()) {
            shoppingCartRepository.save(new ShoppingCart(userId, sku1, price1, price1, ONE, currency));
        }

        CreateOrderRequestDTO dto = new CreateOrderRequestDTO(userId);

        OrderId orderId = given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/api/order")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(OrderId.class);

        // Verify cart is cleared
        assertThat(shoppingCartRepository.findByUserId(userId)).isEmpty();

        // Wait a bit for the order to be created
        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(
            () -> {
                assertThat(orderRepository.findByCustomerId(new CustomerId(userId)))
                    .isNotEmpty()
                    .isEqualTo(List.of(new OrderAggregate(orderId, new CustomerId(userId))));
            }
        );

        // Simulate payment event (since payment microservice is not running in this test)
        // PaymentProcessedEvent is published to Kafka topic paymentEvent
        var createdOrders = orderRepository.findByCustomerId(new CustomerId(userId));
        // Sends mock payment event to paymentEvent Kafka topic
        if (!createdOrders.isEmpty()) {
            OrderAggregate order = createdOrders.getFirst();
            // Mocks successful payment event for order
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(order.getId().value())
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setPaymentId(UUID.randomUUID().toString())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();
            var kafkaTopic = kafkaEventHandler.getKafkaTopic(paymentEvent.getClass());
            kafkaTemplate.send(kafkaTopic, order.getId().value(), paymentEvent);
        }

        // Verify order is created and confirmed (due to mock payment in Kafka listener)
        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<OrderAggregate> orders = orderRepository.findByCustomerId(new CustomerId(userId));
            assertThat(orders).hasSize(1);
            OrderAggregate order = orders.getFirst();
            assertThat(order.getCustomerId().getValue()).isEqualTo(userId);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getItems().getFirst().getProductId().getValue()).isEqualTo(sku1);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(price1);
        });
    }

}

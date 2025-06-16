package com.metao.book.order;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.protobuf.Timestamp;
import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.infrastructure.persistence.repository.JpaOrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDTO;
import com.metao.book.order.presentation.dto.CreateOrderRequest;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.kafka.KafkaEventConfiguration;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.BaseKafkaTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        OrderApplication.class,
        KafkaEventConfiguration.class,
        KafkaEventHandler.class
    })
// For order-microservice
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // To run tests in order
@DirtiesContext // Ensures a clean context for this test class
@ActiveProfiles("test") // Using @Profile("test") instead of @TestPropertySource
class E2EProductPurchaseTest extends BaseKafkaTest {

    @LocalServerPort // Port for order-microservice
    private Integer orderMicroservicePort;

    // Inventory microservice configuration
    private final String inventoryMicroserviceBaseUri = "http://localhost:8083";
    private final boolean checkInventoryService = Boolean.parseBoolean(
        System.getProperty("inventory.service.check", "false"));

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JpaOrderRepository jpaOrderRepository; // For test cleanup

    @Autowired
    private KafkaTemplate<String, OrderPaymentEvent> orderPaymentEventKafkaTemplate;

    @Value("${kafka.topic.order-payment.name}")
    private String orderPaymentTopicName;

    private final String userId = "e2eUser";
    private final String asin1 = "ASIN_E2E_001"; // Assume this product exists in inventory-microservice
    private final BigDecimal price1 = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        // Clean order-microservice DB before each test method execution in this ordered test class
        jpaOrderRepository.deleteAll();
        shoppingCartRepository.deleteAll();

        RestAssured.port = orderMicroservicePort; // Default port for tests in this class
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test method to ensure independence if @Order is removed or for future tests
        jpaOrderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
    }

    @Test
    @Order(1)
    void step1_addProductToCart() {
        // Preliminary check that inventory-microservice has the product (optional)
        if (checkInventoryService) {
            try {
                log.info("🔍 Checking inventory service for product: " + asin1);
                given().baseUri(inventoryMicroserviceBaseUri)
                    .when().get("/products/" + asin1)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("asin", equalTo(asin1))
                    .body("available", equalTo(true));

                log.info("✅ Inventory service check passed for product: " + asin1);

            } catch (Exception e) {
                System.err.println("⚠️  Inventory service check failed for product " + asin1 + ". " +
                    "Proceeding with test assuming product exists. Error: " + e.getMessage());
                // Continue with test - this is just a preliminary check
            }
        } else {
            log.info("ℹ️  Skipping inventory service check (set -Dinventory.service.check=true to enable)");
        }

        AddItemRequestDTO addItemDTO = new AddItemRequestDTO(BigDecimal.ONE, price1, currency);

        given()
            // Port is set in setUp for RestAssured
            .contentType(ContentType.JSON)
            .body(addItemDTO)
        .when()
            .post("/cart/{userId}/{asin}", userId, asin1)
        .then()
            .statusCode(HttpStatus.OK.value());

        assertThat(shoppingCartRepository.findByUserIdAndAsin(userId, asin1)).isPresent();
    }

    @Test
    @Order(2)
    void step2_viewCart() {
        // Ensure item from step 1 is in cart for this ordered test
        // If step1 failed or tests were run out of order, this might fail without this setup.
        if (shoppingCartRepository.findByUserIdAndAsin(userId, asin1).isEmpty()) {
            shoppingCartRepository.save(new ShoppingCart(userId, asin1, price1, price1, BigDecimal.ONE, currency));
        }
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/cart/{userId}", userId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo(userId))
            .body("shopping_cart_items", hasSize(1))
            .body("shopping_cart_items[0].asin", equalTo(asin1));
    }

    @Test
    @Order(3)
    void step3_checkoutAndVerifyOrder() {
        // Ensure item from step 1 is in cart
        if (shoppingCartRepository.findByUserIdAndAsin(userId, asin1).isEmpty()) {
             shoppingCartRepository.save(new ShoppingCart(userId, asin1, price1, price1, BigDecimal.ONE, currency));
        }

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(userId);
        
        @SuppressWarnings("unchecked") // For List.class extraction
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders") // OrderController base path
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().body().jsonPath().getString("value");

        assertThat(orderId).isNotNull(); // Order should be created

        // Verify cart is cleared
        assertThat(shoppingCartRepository.findByUserId(userId)).isEmpty();

        // Wait a bit for the order to be created
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate payment event (since payment microservice is not running in this test)
        List<com.metao.book.order.domain.model.aggregate.Order> createdOrders = orderRepository.findByCustomerId(
            new CustomerId(userId));
        if (!createdOrders.isEmpty()) {
            com.metao.book.order.domain.model.aggregate.Order order = createdOrders.getFirst();
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(order.getId().value())
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL)
                .setPaymentId(UUID.randomUUID().toString())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();
            orderPaymentEventKafkaTemplate.send(orderPaymentTopicName, order.getId().value(), paymentEvent);
        }

        // Verify order is created and confirmed (due to mock payment in Kafka listener)
        Awaitility.await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<com.metao.book.order.domain.model.aggregate.Order> orders = orderRepository.findByCustomerId(
                new CustomerId(userId));
            assertThat(orders).hasSize(1);
            com.metao.book.order.domain.model.aggregate.Order order = orders.getFirst();
            assertThat(order.getCustomerId().getValue()).isEqualTo(userId);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getItems().getFirst().getProductId().getValue()).isEqualTo(asin1);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(price1);
        });
    }
}


package com.metao.book.order;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.application.cart.OrderRepository; // Using the path from previous IT tests
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.presentation.dto.AddItemRequestDTO;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles; // Added import
// import org.springframework.test.context.DynamicPropertyRegistry; // Removed
// import org.springframework.test.context.DynamicPropertySource; // Removed
// import org.testcontainers.containers.PostgreSQLContainer; // Removed
// import org.testcontainers.junit.jupiter.Container; // Removed
import org.testcontainers.junit.jupiter.Testcontainers; // Keep for now, review later

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // For order-microservice
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
               topics = {"${kafka.topic.order-created.name}"}) // Ensure this matches application.properties
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // To run tests in order
@DirtiesContext // Ensures a clean context for this test class
@ActiveProfiles("test") // Added active profile
class E2EProductPurchaseTest {

    @LocalServerPort // Port for order-microservice
    private Integer orderMicroservicePort;
    
    // Hardcoded port for inventory-microservice (assumed to be running independently or via docker-compose)
    private final String inventoryMicroserviceBaseUri = "http://localhost:8083"; 

    // @Container static PostgreSQLContainer<?> postgresOrderDB ... // Removed
    // @DynamicPropertySource static void setProperties(...) ... // Removed

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final String userId = "e2eUser";
    private final String asin1 = "ASIN_E2E_001"; // Assume this product exists in inventory-microservice
    private final BigDecimal price1 = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        // Clean order-microservice DB before each test method execution in this ordered test class
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        
        RestAssured.port = orderMicroservicePort; // Default port for tests in this class
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test method to ensure independence if @Order is removed or for future tests
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
    }

    @Test
    @Order(1)
    void step1_addProductToCart() {
        // Optional: Preliminary check that inventory-microservice has the product
        // This assumes inventory-microservice is running and accessible.
        // In a real E2E setup, ensure this product is seeded or created in inventory service.
        /*
        try {
            given().baseUri(inventoryMicroserviceBaseUri)
                   .when().get("/products/" + asin1)
                   .then().statusCode(HttpStatus.OK.value());
        } catch (Exception e) {
            System.err.println("Warning: Inventory microservice check failed for product " + asin1 + ". Assuming it exists. Error: " + e.getMessage());
            // Proceed with the test, assuming the product exists as per test contract.
        }
        */

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

        CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO(userId);
        
        @SuppressWarnings("unchecked") // For List.class extraction
        List<String> eventIds = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/order") // OrderController base path
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().body().as(List.class);

        assertThat(eventIds).hasSize(1); // One order event for one cart item

        // Verify cart is cleared
        assertThat(shoppingCartRepository.findByUserId(userId)).isEmpty();

        // Verify order is created and confirmed (due to mock payment in Kafka listener)
        Awaitility.await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId);
            assertThat(orders).hasSize(1);
            OrderEntity order = orders.get(0);
            assertThat(order.getCustomerId()).isEqualTo(userId);
            assertThat(order.getProductId()).isEqualTo(asin1);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID); // Changed to PAID
            assertThat(order.getPrice()).isEqualByComparingTo(price1);
        });
    }
}
```

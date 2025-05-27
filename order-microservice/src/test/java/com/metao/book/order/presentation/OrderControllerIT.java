package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.application.cart.OrderRepository; // Corrected path as per file structure
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO; // Record DTO
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.awaitility.Awaitility; // For polling DB for status change
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
               topics = {"${kafka.topic.order-created.name}"}) // Ensure topic name matches application.properties
@DirtiesContext // To ensure Kafka and DB are reset between test classes if needed
class OrderControllerIT {

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("bookstore-order")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        // For EmbeddedKafka, Spring Boot autoconfigures bootstrap servers.
        // Mock schema registry URL if Confluent Protobuf serializer is used by producer
        registry.add("spring.kafka.properties.schema.registry.url", () -> "mock://localhost:8081"); 
    }

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository; 

    private String userId1;
    private String asin1, asin2;
    private Currency currency;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/order"; // Base path for OrderController

        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();

        userId1 = "userOrderTest";
        asin1 = "ASIN_ORDER_001";
        asin2 = "ASIN_ORDER_002";
        currency = Currency.getInstance("USD");

        // Setup cart for the user
        ShoppingCart item1 = new ShoppingCart(userId1, asin1, BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.ONE, currency);
        ShoppingCart item2 = new ShoppingCart(userId1, asin2, BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.valueOf(2), currency);
        shoppingCartRepository.saveAll(List.of(item1, item2));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
    }

    @Test
    void createOrder_whenCartNotEmpty_createsOrdersAndClearsCart() {
        CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO(userId1);

        @SuppressWarnings("unchecked") // Suppress warning for List class extraction
        List<String> eventIds = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post() 
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().body().as(List.class);

        assertThat(eventIds).hasSize(2); 

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserId(userId1);
        assertThat(cartItems).isEmpty();

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId1); 
            assertThat(orders).hasSize(2);
            orders.forEach(order -> {
                assertThat(order.getCustomerId()).isEqualTo(userId1);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED); // Kafka listener updates to CONFIRMED
                if(order.getProductId().equals(asin1)) {
                    assertThat(order.getQuantity()).isEqualByComparingTo(BigDecimal.ONE);
                    assertThat(order.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10));
                } else if (order.getProductId().equals(asin2)) {
                    assertThat(order.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
                    assertThat(order.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20));
                }
            });
        });
    }

    @Test
    void createOrder_whenCartIsEmpty_returnsBadRequest() {
        String emptyUserId = "emptyCartUser";
        // Ensure this user has no cart items
        shoppingCartRepository.deleteByUserId(emptyUserId); 

        CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO(emptyUserId); 

        given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("get(0)", containsString("Cart is empty")); // Accessing the first element of the error list
    }
    
    @Test
    void getOrderByOrderId_whenOrderExistsAndConfirmed_returnsOrder() {
        CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO(userId1);
        given().contentType(ContentType.JSON).body(createOrderRequest).when().post().then().statusCode(HttpStatus.OK.value());

        final OrderEntity[] fetchedOrderContainer = new OrderEntity[1];
        
        // Wait for orders to be created and processed by Kafka listener
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId1);
            assertThat(orders).hasSize(2); // Ensure both orders are processed
            Optional<OrderEntity> orderToFetch = orders.stream()
                                                       .filter(o -> o.getProductId().equals(asin1) && o.getStatus() == OrderStatus.CONFIRMED)
                                                       .findFirst();
            assertThat(orderToFetch).isPresent();
            fetchedOrderContainer[0] = orderToFetch.get();
        });
        
        String actualOrderId = fetchedOrderContainer[0].getOrderId();

        // The OrderController's getOrderByOrderId expects @RequestParam("order_id")
        given()
            .contentType(ContentType.JSON)
            .queryParam("order_id", actualOrderId) // Correctly pass as query parameter
        .when()
            .get() // The path is just /order as per controller's @RequestMapping
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("order_id", equalTo(actualOrderId))
            .body("product_id", equalTo(asin1))
            .body("customer_id", equalTo(userId1))
            .body("status", equalTo(OrderStatus.CONFIRMED.name()));
    }
}
```

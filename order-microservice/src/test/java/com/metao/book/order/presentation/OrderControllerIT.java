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
import com.metao.book.order.OrderPaymentEvent; // Added import
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer; // Added import
import org.apache.kafka.clients.producer.ProducerConfig; // Added import
import org.apache.kafka.common.serialization.StringSerializer; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration; // Added import
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean; // Added import
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaProducerFactory; // Added import
import org.springframework.kafka.core.KafkaTemplate; // Added import
import org.springframework.kafka.core.ProducerFactory; // Added import
import org.springframework.kafka.test.EmbeddedKafkaBroker; // Added import
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils; // Added import
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.HashMap; // Added import
import java.util.List;
import java.util.Map; // Added import
import java.util.Optional;
import java.util.UUID; // For generating paymentId

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, 
               brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" }, // Changed port
               topics = {
                   "${kafka.topic.order-created.name}", 
                   "${kafka.topic.order-payment.name}" // Added payment topic
               })
@DirtiesContext // To ensure Kafka and DB are reset between test classes if needed
class OrderControllerIT {

    // TestConfiguration for OrderPaymentEvent producer
    @TestConfiguration
    static class KafkaTestProducerConfiguration {
        @Bean
        public ProducerFactory<String, OrderPaymentEvent> orderPaymentEventProducerFactory(EmbeddedKafkaBroker broker) {
            Map<String, Object> configProps = new HashMap<>(KafkaTestUtils.producerProps(broker));
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
            // Assuming schema registry URL is picked from DynamicPropertySource 
            // or is not strictly needed by serializer in this test context.
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, OrderPaymentEvent> orderPaymentEventKafkaTemplate(
                ProducerFactory<String, OrderPaymentEvent> orderPaymentEventProducerFactory) {
            return new KafkaTemplate<>(orderPaymentEventProducerFactory);
        }
    }

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
        // Mock schema registry URL for Confluent Protobuf serializer if used by producer/consumer.
        registry.add("spring.kafka.properties.schema.registry.url", () -> "mock://localhost:8081"); 
    }

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired // Autowire the new KafkaTemplate for sending payment events
    private KafkaTemplate<String, OrderPaymentEvent> orderPaymentEventKafkaTemplate;

    @Value("${kafka.topic.order-payment.name}") // For sending payment events
    private String orderPaymentTopicName;

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

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId1);
            assertThat(orders).hasSize(2);
            orders.forEach(order -> {
                assertThat(order.getCustomerId()).isEqualTo(userId1);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW); // Initial status
            });
        });

        // Simulate payment events for each order
        List<OrderEntity> createdOrders = orderRepository.findAllByCustomerId(userId1);
        for (OrderEntity order : createdOrders) {
            OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                    .setOrderId(order.getOrderId())
                    .setPaymentId(UUID.randomUUID().toString())
                    .setStatus(OrderPaymentEvent.Status.SUCCESSFUL) // Simulate successful payment
                    .setProcessedTimestamp(System.currentTimeMillis())
                    .build();
            orderPaymentEventKafkaTemplate.send(orderPaymentTopicName, order.getOrderId(), paymentEvent);
        }

        // Await final status (PAID)
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId1);
            assertThat(orders).hasSize(2);
            orders.forEach(order -> {
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID); // Final status after payment
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
        
        // Wait for orders to be created with initial NEW status
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<OrderEntity> orders = orderRepository.findAllByCustomerId(userId1);
            assertThat(orders).hasSize(2);
            Optional<OrderEntity> orderToProcessOpt = orders.stream()
                                                       .filter(o -> o.getProductId().equals(asin1) && o.getStatus() == OrderStatus.NEW)
                                                       .findFirst();
            assertThat(orderToProcessOpt).isPresent();
            fetchedOrderContainer[0] = orderToProcessOpt.get();
        });
        
        String orderIdToConfirm = fetchedOrderContainer[0].getOrderId();

        // Simulate successful payment event for this specific order
        OrderPaymentEvent paymentEvent = OrderPaymentEvent.newBuilder()
                .setOrderId(orderIdToConfirm)
                .setPaymentId(UUID.randomUUID().toString())
                .setStatus(OrderPaymentEvent.Status.SUCCESSFUL) // Simulate successful payment
                .setProcessedTimestamp(System.currentTimeMillis())
                .build();
        orderPaymentEventKafkaTemplate.send(orderPaymentTopicName, orderIdToConfirm, paymentEvent);

        // Wait for the specific order to be updated to PAID
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<OrderEntity> updatedOrderOpt = orderRepository.findByOrderId(orderIdToConfirm);
            assertThat(updatedOrderOpt).isPresent();
            assertThat(updatedOrderOpt.get().getStatus()).isEqualTo(OrderStatus.PAID);
        });

        // Now fetch the order via controller and verify
        given()
            .contentType(ContentType.JSON)
            .queryParam("order_id", orderIdToConfirm)
        .when()
            .get() 
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("order_id", equalTo(orderIdToConfirm))
            .body("product_id", equalTo(asin1))
            .body("customer_id", equalTo(userId1))
            .body("status", equalTo(OrderStatus.PAID.name())); // Expect PAID status
    }
}
```

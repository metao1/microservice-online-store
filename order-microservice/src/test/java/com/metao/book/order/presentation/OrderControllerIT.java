package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.google.protobuf.Timestamp;
import com.metao.book.order.application.cart.OrderRepository;
import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.shared.OrderPaymentEvent;
import com.metao.shared.test.BaseKafkaTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext // To ensure Kafka and DB are reset between test classes if needed
class OrderControllerIT extends BaseKafkaTest {

    @LocalServerPort
    private Integer port;

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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/order"; // Base path for OrderController

        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();

        userId1 = "userOrderTest";
        asin1 = "ASIN_ORDER_001";
        asin2 = "ASIN_ORDER_002";
        Currency currency = Currency.getInstance("USD");

        // Setup cart for the user
        ShoppingCart item1 = new ShoppingCart(userId1, asin1, BigDecimal.valueOf(10), BigDecimal.valueOf(10),
            BigDecimal.ONE,
            currency);
        ShoppingCart item2 = new ShoppingCart(userId1, asin2, BigDecimal.valueOf(20), BigDecimal.valueOf(20),
            BigDecimal.valueOf(2),
            currency);
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
                .setId(order.getOrderId())
                    .setStatus(OrderPaymentEvent.Status.SUCCESSFUL) // Simulate successful payment
                    .setPaymentId(UUID.randomUUID().toString())
                    .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
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
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
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


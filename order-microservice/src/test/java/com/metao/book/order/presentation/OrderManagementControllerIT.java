package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.matchesPattern;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for OrderController
 */
@ActiveProfiles("test")
@DisplayName("OrderController Integration Tests")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderManagementControllerIT extends KafkaContainer {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private final String userId = "customer123";
    private final String sku = "SKU_E2E_001"; // Assume this product exists in inventory-microservice
    private final BigDecimal unitPrice = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("EUR");

    @LocalServerPort
    private Integer port;

    @Autowired
    ConsumerFactory<String, OrderUpdatedEvent> consumerFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, OrderUpdatedEvent> kafkaTemplate;
    @Autowired
    KafkaEventHandler eventHandler;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() {
            // Given
            var dto = new CreateOrderRequestDTO("customer123");

            shoppingCartService.addItemToCart(
                "customer123",
                Set.of(new ShoppingCartItem(sku, ONE, unitPrice, currency))
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(dto)
                .post("/api/order")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("value", matchesPattern("[a-f0-9\\-]+"));// UUID generated orderId in the service
        }

        @Test
        @DisplayName("Should add item to orders successfully")
        void shouldAddItemToOrdersSuccessfully() {
            // GIVEN
            var orderId = new OrderId("order123");
            // Assume this product exists in inventory-microservice
            AddItemRequestDto requestDto = new AddItemRequestDto(userId,
                Set.of(new ShoppingCartItem(sku, ONE, BigDecimal.valueOf(12.99), Currency.getInstance("EUR")))
            );
            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId(userId));

            orderRepository.save(orderAggregate);

            given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .put("/api/order/{orderId}/items", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        }

        @Test
        @DisplayName("Process updating of an order to different status successfully")
        void shouldUpdateStatusOfAnOrderSuccessfully() {
            // GIVEN
            var orderId = new OrderId("order123");
            var orderAggregate = new OrderAggregate(orderId, new CustomerId(userId));
            orderRepository.save(orderAggregate);

            var paidStatusUpdate = new UpdateStatusRequestDto("PAID");
            // Should update from CREATED to PAID successfully

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(paidStatusUpdate)
                .patch("/api/order/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

            // Should be able to update from PAID to CANCEL
            var cancelStatusUpdate = new UpdateStatusRequestDto("CANCELLED");
            given()
                .contentType(ContentType.JSON)
                .body(cancelStatusUpdate)
                .patch("/api/order/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());
        }
    }

    @Nested
    @DisplayName("Order Item Management")
    class OrderItemManagement {

        @Test
        @DisplayName("Should add items to order successfully")
        void shouldAddItemToOrderSuccessfully() {
            // Given
            var orderId = new OrderId("order123");
            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId(userId));
            orderRepository.save(orderAggregate);
            AddItemRequestDto addItemRequestDto = new AddItemRequestDto(userId,
                Set.of(new ShoppingCartItem(sku, ONE, unitPrice, currency))
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(addItemRequestDto)
                .put("/api/order/{orderId}/items", orderId.value())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

            OrderAggregate updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getDomainEvents())
                .size()
                .isEqualTo(2);

            String kafkaTopic = eventHandler.getKafkaTopic(OrderUpdatedEvent.class);

            await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(kafkaTopic);
                    int partition = Math.abs(orderId.hashCode()) % partitions.size();
                    kafkaTemplate.setConsumerFactory(consumerFactory);
                    var event = kafkaTemplate.receive(kafkaTopic, partition, 0);
                    assertThat(event)
                        .isNotNull()
                        .extracting(ConsumerRecord::value)
                        .satisfies(orderUpdatedEvent -> {
                            assertThat(orderUpdatedEvent.getId()).isEqualTo("order123");
                            assertThat(orderUpdatedEvent.getProductId()).isEqualTo(sku);
                            assertThat(orderUpdatedEvent.getQuantity()).isEqualTo(1.0);
                            assertThat(orderUpdatedEvent.getPrice()).isEqualTo(12.99);
                            assertThat(orderUpdatedEvent.getCurrency()).isEqualTo("EUR");
                            assertThat(orderUpdatedEvent.hasUpdateTime()).isTrue();
                        });
                });
        }
    }

    @Nested
    @DisplayName("Order Queries")
    class OrderQueries {

        @Test
        @DisplayName("Should get customer orders successfully")
        void shouldGetCustomerOrdersSuccessfully() throws Exception {
            // Given
            // Given
            var orderId = new OrderId("order123");
            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId("customer123"));
            orderRepository.save(orderAggregate);
            AddItemRequestDto addItemRequestDto = new AddItemRequestDto(userId,
                Set.of(new ShoppingCartItem(sku, ONE, unitPrice, currency))
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(addItemRequestDto)
                .get("/api/order/customer/{customerId}", orderId.value())
                .then()
                .statusCode(HttpStatus.OK.value());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        private static final String FOUND_ORDER_123 = "order123";
        private static final String NOT_FOUND_ORDER_999 = "order999";

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() {
            // Given
            AddItemRequestDto addItemRequestDto = new AddItemRequestDto(userId,
                Set.of(new ShoppingCartItem(sku, ONE, unitPrice, currency))
            );

            // When & Then
            // Since there's no global exception handler, the exception will be wrapped in ServletException
            // We verify that the exception is thrown and contains our original message
            given()
                .contentType(ContentType.JSON)
                .body(addItemRequestDto)
                .put("/api/order/{orderId}/items", NOT_FOUND_ORDER_999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

            given()
                .contentType(ContentType.JSON)
                .put("/api/order/{orderId}/items", FOUND_ORDER_123)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}

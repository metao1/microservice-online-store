package com.metao.book.order;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.protobuf.Timestamp;
import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.application.port.OrderRepository;
import com.metao.book.order.infrastructure.persistence.cart.ShoppingCartRepository;
import com.metao.book.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.metao.book.order.presentation.dto.CreateOrderRequestDTO;
import com.metao.book.shared.InventoryReductionRequestedEvent;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainerBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.mockito.Mockito;

@Slf4j
@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class E2EProductPurchaseContainerBaseIT extends KafkaContainerBase {

    private final String userId = "e2eUser";
    private final String sku1 = "SKU_E2E_001";
    private final String productTitle = "productTitle";
    private final BigDecimal quantity1 = BigDecimal.ONE;
    private final BigDecimal price1 = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("EUR");
    private final String userToken = "e2e-mock-jwt-token";

    private final ConcurrentLinkedQueue<ConsumerRecord<String, InventoryReductionRequestedEvent>> inventoryReductionEvents =
        new ConcurrentLinkedQueue<>();

    @LocalServerPort
    private Integer orderMicroservicePort;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingCartCommandPort shoppingCartCommandPort;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataOrderRepository jpaOrderRepository;

    @Autowired
    private KafkaTemplate<String, OrderPaymentUpdatedEvent> kafkaTemplate;

    @Autowired
    private KafkaEventHandler kafkaEventHandler;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        jpaOrderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        inventoryReductionEvents.clear();
        RestAssured.port = orderMicroservicePort;
        Mockito.when(jwtDecoder.decode(userToken)).thenReturn(
            Jwt.withTokenValue(userToken)
                .header("alg", "none")
                .subject(userId)
                .audience(List.of("account"))
                .claim("roles", List.of("CUSTOMER"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        );
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    void shouldCompletePurchaseFlowAndPublishInventoryReductionEvent() {
        List<ShoppingCartItem> addItemDTO = List.of(
            new ShoppingCartItem(sku1, productTitle, quantity1, price1, currency)
        );

        assertThat(shoppingCartCommandPort.addItemToCart(userId, addItemDTO)).isEqualTo(1);

        assertThat(shoppingCartRepository.findByUserIdAndSku(userId, sku1)).isPresent();

        OrderId orderId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(new CreateOrderRequestDTO(userId))
            .when()
            .post("/api/order")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(OrderId.class);

        awaitOrderCreated(orderId);

        assertThat(shoppingCartRepository.findByUserId(userId)).hasSize(1);

        OrderPaymentUpdatedEvent paymentEvent = OrderPaymentUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setOrderId(orderId.value())
            .setStatus(Status.SUCCESSFUL)
            .setPaymentId(UUID.randomUUID().toString())
            .setUpdatedTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build();

        kafkaTemplate.executeInTransaction(template -> {
            try {
                template.send(kafkaEventHandler.getKafkaTopic(paymentEvent.getClass()), orderId.value(), paymentEvent)
                    .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to publish payment event", e);
            }
            return null;
        });

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(300)).untilAsserted(() -> {
            OrderAggregate order = orderRepository.findById(orderId).orElseThrow();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getItems().getFirst().getProductSku().value()).isEqualTo(sku1);
            assertThat(order.getTotal().fixedPointAmount()).isPositive();
            assertThat(shoppingCartRepository.findByUserId(userId)).isEmpty();
        });

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(300)).untilAsserted(() ->
            assertThat(countInventoryReductionEvents(sku1)).isEqualTo(1)
        );
    }

    @Test
    void shouldNotPublishDuplicateInventoryReductionEventForDuplicateSuccessfulPayment() {
        List<ShoppingCartItem> addItemDTO = List.of(
            new ShoppingCartItem(sku1, productTitle, quantity1, price1, currency)
        );

        assertThat(shoppingCartCommandPort.addItemToCart(userId, addItemDTO)).isEqualTo(1);

        OrderId orderId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(new CreateOrderRequestDTO(userId))
            .when()
            .post("/api/order")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(OrderId.class);

        awaitOrderCreated(orderId);

        String paymentId = UUID.randomUUID().toString();
        OrderPaymentUpdatedEvent firstPaymentEvent = OrderPaymentUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setOrderId(orderId.value())
            .setStatus(Status.SUCCESSFUL)
            .setPaymentId(paymentId)
            .setUpdatedTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build();

        kafkaTemplate.send(kafkaEventHandler.getKafkaTopic(firstPaymentEvent.getClass()), orderId.value(), firstPaymentEvent);

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(300)).untilAsserted(() -> {
            OrderAggregate order = orderRepository.findById(orderId).orElseThrow();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        });

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(300)).untilAsserted(() ->
            assertThat(countInventoryReductionEvents(sku1)).isEqualTo(1)
        );

        OrderPaymentUpdatedEvent duplicatePaymentEvent = OrderPaymentUpdatedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setOrderId(orderId.value())
            .setStatus(Status.SUCCESSFUL)
            .setPaymentId(paymentId)
            .setUpdatedTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build();

        kafkaTemplate.send(kafkaEventHandler.getKafkaTopic(duplicatePaymentEvent.getClass()), orderId.value(),
            duplicatePaymentEvent);

        await()
            .during(Duration.ofSeconds(3))
            .atMost(Duration.ofSeconds(8))
            .pollInterval(Duration.ofMillis(300))
            .untilAsserted(() -> assertThat(countInventoryReductionEvents(sku1)).isEqualTo(1));
    }

    @KafkaListener(
        id = "e2e-inventory-reduction-listener-${random.uuid}",
        topics = "${kafka.topic.inventory-reduction-requested.name}",
        groupId = "e2e-inventory-reduction-group",
        properties = {
            "specific.protobuf.value.type=com.metao.book.shared.InventoryReductionRequestedEvent"
        }
    )
    void onInventoryReductionRequestedEvent(ConsumerRecord<String, InventoryReductionRequestedEvent> event) {
        inventoryReductionEvents.add(event);
    }

    private long countInventoryReductionEvents(String sku) {
        return inventoryReductionEvents.stream()
            .filter(record -> sku.equals(record.value().getSku()))
            .count();
    }

    private void awaitOrderCreated(OrderId orderId) {
        await().atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofMillis(300))
            .untilAsserted(() -> {
                var maybeOrder = orderRepository.findById(orderId);
                assertThat(maybeOrder).isPresent();
                OrderAggregate order = maybeOrder.orElseThrow();
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            });
    }
}

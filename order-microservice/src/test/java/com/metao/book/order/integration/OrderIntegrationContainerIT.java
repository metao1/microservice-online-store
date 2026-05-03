package com.metao.book.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.service.OrderManagementService;
import com.metao.book.order.infrastructure.persistence.mapper.OrderEntityMapper;
import com.metao.book.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.metao.book.order.presentation.dto.OrderResponseDto;
import com.metao.shared.test.KafkaContainer;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
class OrderIntegrationContainerIT extends KafkaContainer {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final BigDecimal ONE = BigDecimal.ONE;

    @Autowired
    private OrderManagementService orderService;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    private final String sku = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        shoppingCartService.clearCart("user123");
    }

    private OrderId createOrderWithCartItems(UserId userId, Set<ShoppingCartItem> items) {
        shoppingCartService.addItemToCart(userId.value(), items);
        OrderId orderId = orderService.createOrder(userId);
        awaitOrderPersisted(orderId);
        return orderId;
    }

    private void awaitOrderPersisted(OrderId orderId) {
        await().atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofMillis(300))
            .untilAsserted(() -> assertThat(orderRepository.findById(orderId.value())).isPresent());
    }

    private OrderId createOrderWithCartItems(
        UserId userId,
        String productSku,
        String title,
        BigDecimal quantity,
        BigDecimal price
    ) {
        return createOrderWithCartItems(
            userId,
            Set.of(new ShoppingCartItem(productSku, title, quantity, price, EUR))
        );
    }

    @Nested
    class OrderCreationTests {

        static Stream<Arguments> invalidUserIdTestData() {
            return Stream.of(Arguments.of((UserId) null));
        }

        @Test
        void shouldCreateOrderAndPublishEvent() {
            UserId userId = UserId.of("user123");

            OrderId orderId = createOrderWithCartItems(
                userId,
                sku,
                "product123",
                BigDecimal.TWO,
                BigDecimal.valueOf(10)
            );

            assertThat(orderId).isNotNull();
            assertThat(orderRepository.findById(orderId.value())).isPresent();
        }

        @ParameterizedTest
        @MethodSource("invalidUserIdTestData")
        void shouldNotCreateOrderWithInvalidUserId(UserId userId) {
            assertThatThrownBy(() -> orderService.createOrder(userId))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldCreateMultipleOrdersForSameCustomer() {
            UserId userId = UserId.of("user123");

            OrderId firstOrderId = createOrderWithCartItems(
                userId,
                "product-1",
                "product123",
                BigDecimal.TWO,
                BigDecimal.valueOf(10)
            );

            shoppingCartService.clearCart(userId.value());

            OrderId secondOrderId = createOrderWithCartItems(
                userId,
                "product-2",
                "product456",
                BigDecimal.ONE,
                BigDecimal.valueOf(15)
            );

            assertThat(firstOrderId).isNotEqualTo(secondOrderId);
            assertThat(orderRepository.findAll()).hasSize(2);
        }

        @Test
        void shouldCreateOrderWithMultipleCartItems() {
            UserId userId = UserId.of("user123");

            OrderId orderId = createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.TWO, BigDecimal.valueOf(10), EUR),
                    new ShoppingCartItem("product-2", "Book 2", BigDecimal.ONE, BigDecimal.valueOf(15), EUR)
                )
            );

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(41.65));
        }
    }

    @Nested
    class OrderItemManagementTests {

        @Test
        void shouldUpdateItemQuantity() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", BigDecimal.TWO, BigDecimal.TEN);
            var beforeUpdate = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            var initialQuantity = beforeUpdate.getItems().getFirst().getQuantity().value();
            var unitPrice = beforeUpdate.getItems().getFirst().getUnitPrice().fixedPointAmount();
            var initialTotal = beforeUpdate.getTotal().fixedPointAmount();

            orderService.updateItemQuantity(orderId);

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems().getFirst().getQuantity().value())
                .isGreaterThanOrEqualTo(initialQuantity);
            assertThat(order.getTotal().fixedPointAmount())
                .isGreaterThanOrEqualTo(initialTotal);
        }

        @Test
        void shouldRemoveItem() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            orderService.removeItem(orderId, com.metao.book.shared.domain.product.ProductSku.of(sku));

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems()).isEmpty();
            assertThat(order.getTotal()).isNull();
        }
    }

    @Nested
    class OrderStatusManagementTests {

        static Stream<Arguments> validStatusTransitionTestData() {
            return Stream.of(Arguments.of(List.of(OrderStatus.PAID)));
        }

        @Test
        void shouldUpdateOrderStatusAndPublishEvent() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            orderService.updateOrderStatus(orderId, OrderStatus.PAID.name());

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        void shouldNotUpdateStatusOfNonExistentOrder() {
            OrderId nonExistentOrderId = OrderId.generate();

            assertThatThrownBy(() -> orderService.updateOrderStatus(nonExistentOrderId, OrderStatus.PAID.name()))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("order not found with id: %s".formatted(nonExistentOrderId.value()));
        }

        @Test
        void shouldNotUpdateStatusWithInvalidStatus() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @MethodSource("validStatusTransitionTestData")
        void shouldFollowValidStatusTransition(List<OrderStatus> statusSequence) {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            for (OrderStatus status : statusSequence) {
                orderService.updateOrderStatus(orderId, status.name());
                var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
                assertThat(order.getStatus()).isEqualTo(status);
            }
        }

        @Test
        void shouldNotAllowInvalidStatusTransition() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);
            orderService.updateOrderStatus(orderId, OrderStatus.PAID.name());

            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.CREATED.name()))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    class OrderQueryTests {

        @Test
        void shouldGetCustomerOrders() {
            UserId userId = UserId.of("user123");
            createOrderWithCartItems(userId, "product-1", "Book 1", ONE, BigDecimal.TEN);

            shoppingCartService.clearCart(userId.value());
            createOrderWithCartItems(userId, "product-2", "Book 2", BigDecimal.TWO, BigDecimal.valueOf(15));

            List<OrderResponseDto> orders = orderService.getCustomerOrders(userId).stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            assertThat(orders).hasSize(2);
            assertThat(orders).allSatisfy(order -> assertThat(order.getUserId()).isEqualTo(userId.value()));
        }

        @Test
        void shouldReturnEmptyListForNonExistentCustomer() {
            List<OrderResponseDto> orders = orderService.getCustomerOrders(UserId.of("nonExistentCustomer"))
                .stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            assertThat(orders).isEmpty();
        }

        @Test
        void shouldGetOrdersWithCorrectTotal() {
            UserId userId = UserId.of("user123");
            createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-0", "Book 0", BigDecimal.ONE, BigDecimal.valueOf(5), EUR),
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.valueOf(2), BigDecimal.valueOf(10), EUR),
                    new ShoppingCartItem("product-2", "Book 2", BigDecimal.ONE, BigDecimal.valueOf(15), EUR)
                )
            );

            List<OrderResponseDto> orders = orderService.getCustomerOrders(userId).stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            assertThat(orders).hasSize(1);
            assertThat(orders.getFirst().getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(47.60));
        }

        @Test
        void shouldGetOrdersByStatus() {
            UserId userId = UserId.of("user123");
            OrderId paidOrderId = createOrderWithCartItems(userId, "product-1", "Book 1", ONE, BigDecimal.TEN);

            shoppingCartService.clearCart(userId.value());
            createOrderWithCartItems(userId, "product-2", "Book 2", ONE, BigDecimal.valueOf(15));

            orderService.updateOrderStatus(paidOrderId, OrderStatus.PAID.name());

            List<OrderResponseDto> allOrders = orderService.getCustomerOrders(userId).stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            List<OrderResponseDto> paidOrders = allOrders.stream()
                .filter(order -> OrderStatus.PAID.name().equals(order.getStatus()))
                .toList();
            List<OrderResponseDto> createdOrders = allOrders.stream()
                .filter(order -> OrderStatus.CREATED.name().equals(order.getStatus()))
                .toList();

            assertThat(allOrders).hasSize(2);
            assertThat(paidOrders).hasSize(1);
            assertThat(createdOrders).hasSize(1);
        }
    }

    @Nested
    class TransactionManagementTests {

        @Test
        void shouldMaintainConsistencyAcrossOperations() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-0", "Book 0", BigDecimal.ONE, BigDecimal.valueOf(5), EUR),
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.valueOf(2), BigDecimal.valueOf(10), EUR)
                )
            );

            orderService.updateOrderStatus(orderId, OrderStatus.PAID.name());

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(29.75));
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandleLargeOrderQuantities() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(
                userId,
                "product-1",
                "Bulk Book",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10)
            );

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(11900));
        }

        @Test
        void shouldHandleHighPrecisionPrices() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(
                userId,
                "product-1",
                "Precise Book",
                BigDecimal.ONE,
                BigDecimal.valueOf(10.999)
            );

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems().getFirst().getUnitPrice().fixedPointAmount()).isEqualByComparingTo(
                BigDecimal.valueOf(10.999)
            );
        }

        @Test
        void shouldHandleSpecialCharactersInProductNames() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(
                userId,
                "product-1",
                "Product!@#$%^&*()_+",
                BigDecimal.ONE,
                BigDecimal.TEN
            );

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems().getFirst().getTitle().value()).isEqualTo("Product!@#$%^&*()_+");
        }

        @Test
        void shouldHandleLongUserIds() {
            String longUserId = "customer" + "x".repeat(100);
            UserId userId = UserId.of(longUserId);

            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getUserId().value()).isEqualTo(longUserId);
        }
    }
}

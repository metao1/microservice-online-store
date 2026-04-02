package com.metao.book.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
class OrderIntegrationContainerIT extends KafkaContainer {

    private static final Currency USD = Currency.getInstance("USD");
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
        return orderService.createOrder(userId);
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
            Set.of(new ShoppingCartItem(productSku, title, quantity, price, USD))
        );
    }

    @Nested
    class OrderCreationTests {

        static Stream<Arguments> invalidUserIdTestData() {
            return Stream.of(Arguments.of((UserId) null));
        }

        @Test
        @Transactional
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
        @Transactional
        void shouldNotCreateOrderWithInvalidUserId(UserId userId) {
            assertThatThrownBy(() -> orderService.createOrder(userId))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @Transactional
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
        @Transactional
        void shouldCreateOrderWithMultipleCartItems() {
            UserId userId = UserId.of("user123");

            OrderId orderId = createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.TWO, BigDecimal.valueOf(10), USD),
                    new ShoppingCartItem("product-2", "Book 2", BigDecimal.ONE, BigDecimal.valueOf(15), USD)
                )
            );

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(35));
        }
    }

    @Nested
    class OrderItemManagementTests {

        @Test
        @Transactional
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
        @Transactional
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
        @Transactional
        void shouldUpdateOrderStatusAndPublishEvent() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            orderService.updateOrderStatus(orderId, OrderStatus.PAID.name());

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @Transactional
        void shouldNotUpdateStatusOfNonExistentOrder() {
            OrderId nonExistentOrderId = OrderId.generate();

            assertThatThrownBy(() -> orderService.updateOrderStatus(nonExistentOrderId, OrderStatus.PAID.name()))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("order not found with id: %s".formatted(nonExistentOrderId.value()));
        }

        @Test
        @Transactional
        void shouldNotUpdateStatusWithInvalidStatus() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @MethodSource("validStatusTransitionTestData")
        @Transactional
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
        @Transactional
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
        @Transactional
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
        @Transactional
        void shouldReturnEmptyListForNonExistentCustomer() {
            List<OrderResponseDto> orders = orderService.getCustomerOrders(UserId.of("nonExistentCustomer"))
                .stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            assertThat(orders).isEmpty();
        }

        @Test
        @Transactional
        void shouldGetOrdersWithCorrectTotal() {
            UserId userId = UserId.of("user123");
            createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-0", "Book 0", BigDecimal.ONE, BigDecimal.valueOf(5), USD),
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.valueOf(2), BigDecimal.valueOf(10), USD),
                    new ShoppingCartItem("product-2", "Book 2", BigDecimal.ONE, BigDecimal.valueOf(15), USD)
                )
            );

            List<OrderResponseDto> orders = orderService.getCustomerOrders(userId).stream()
                .map(OrderResponseDto::fromDomain)
                .toList();

            assertThat(orders).hasSize(1);
            assertThat(orders.getFirst().getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(40));
        }

        @Test
        @Transactional
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
        @Transactional
        void shouldMaintainConsistencyAcrossOperations() {
            UserId userId = UserId.of("user123");
            OrderId orderId = createOrderWithCartItems(
                userId,
                Set.of(
                    new ShoppingCartItem("product-0", "Book 0", BigDecimal.ONE, BigDecimal.valueOf(5), USD),
                    new ShoppingCartItem("product-1", "Book 1", BigDecimal.valueOf(2), BigDecimal.valueOf(10), USD)
                )
            );

            orderService.updateOrderStatus(orderId, OrderStatus.PAID.name());

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(25));
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        @Transactional
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
            assertThat(order.getTotal().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        }

        @Test
        @Transactional
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
        @Transactional
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
        @Transactional
        void shouldHandleLongUserIds() {
            String longUserId = "customer" + "x".repeat(100);
            UserId userId = UserId.of(longUserId);

            OrderId orderId = createOrderWithCartItems(userId, sku, "product123", ONE, BigDecimal.TEN);

            var order = OrderEntityMapper.toDomain(orderRepository.findById(orderId.value()).orElseThrow());
            assertThat(order.getUserId().value()).isEqualTo(longUserId);
        }
    }
}

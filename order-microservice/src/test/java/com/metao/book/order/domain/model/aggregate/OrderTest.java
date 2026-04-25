package com.metao.book.order.domain.model.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.order.domain.exception.OrderStateTransitionNotAllowed;
import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.order.domain.model.event.DomainOrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OrderTest {

    @Nested
    class OrderCreation {

        static Stream<Arguments> orderConstructorNullTestData() {
            return Stream.of(
                Arguments.of(null, UserId.of("user123"), "id can't be null"),
                Arguments.of(OrderId.generate(), null, "userId can't be null")
            );
        }

        @Test
        void shouldCreateOrderWithInitialState() {
            // Given
            OrderId orderId = OrderId.generate();
            UserId userId = UserId.of("user123");

            // When
            OrderAggregate order = new OrderAggregate(orderId, userId);

            // Then
            assertThat(order.getId()).isEqualTo(orderId);
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getItems()).isEmpty();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getCreatedAt()).isNotNull();
            assertThat(order.getUpdatedAt()).isNotNull();

            // Verify events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(0);
        }

        @ParameterizedTest
        @MethodSource("orderConstructorNullTestData")
        void shouldThrowExceptionForNullParameters(
            OrderId orderId,
            UserId userId,
            String expectedMessage
        ) {
            assertThatThrownBy(() -> new OrderAggregate(orderId, userId))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
        }
    }

    @Nested
    class OrderItemManagement {

        @Test
        void shouldCreateOrder() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));

            ProductSku productSku = ProductSku.of("product123");
            ProductTitle productTitle = new ProductTitle("product123");
            Quantity quantity = Quantity.of(BigDecimal.valueOf(2.0));
            Money unitPrice = Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(10.0));

            // When
            order.addItem(productSku, productTitle, quantity, unitPrice);

            // Then
            assertThat(order.getItems()).hasSize(1);
            OrderItem orderItem = order.getItems().getFirst();
            assertThat(orderItem.getProductSku()).isEqualTo(productSku);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
            assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);

            // Verify that item mutation itself does not publish creation events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).isEmpty();

            order.raiseOrderCreatedEvents();

            events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(DomainOrderCreatedEvent.class);

            DomainOrderCreatedEvent orderCreatedEvent = (DomainOrderCreatedEvent) events.getFirst();
            assertThat(orderCreatedEvent.getOrderId()).isNotNull();
            assertThat(orderCreatedEvent.getTotal()).isEqualTo(
                Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(20.0)));
            assertThat(orderCreatedEvent.getUserId()).extracting(UserId::toString).isEqualTo("user123");
        }

        @Test
        void shouldThrowExceptionForNullproductSku() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            assertThatThrownBy(() -> order.addItem(null,
                ProductTitle.of("product-123"),
                Quantity.of(BigDecimal.ONE),
                Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(10.0))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("productSku can't be null");
        }

        @Test
        void shouldThrowExceptionForNegativeQuantity() {
            assertThatThrownBy(() -> Quantity.of(BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        // Note: Shared Money class doesn't validate negative amounts in constructor
    }

    @Nested
    class OrderStatusManagement {

        static Stream<Arguments> invalidStatusTransitions() {
            return Stream.of(
                Arguments.of(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT,
                    "Transition error: Cannot transition from CREATED to PENDING_PAYMENT"),
                Arguments.of(OrderStatus.CREATED, OrderStatus.PROCESSING,
                    "Transition error: Cannot transition from CREATED to PROCESSING")
            );
        }

        @Test
        void shouldUpdateOrderStatus() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(),
                UserId.of("user123"));

            OrderStatus newStatus = OrderStatus.PAID;

            // When
            order.updateStatus(newStatus);

            // Then
            assertThat(order.getStatus()).isEqualTo(newStatus);

            // Verify events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(DomainOrderStatusChangedEvent.class);

            DomainOrderStatusChangedEvent statusEvent = (DomainOrderStatusChangedEvent) events.getFirst();
            assertThat(statusEvent.getOldStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(statusEvent.getNewStatus()).isEqualTo(newStatus);
        }

        @Test
        void shouldThrowExceptionForNullStatus() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            assertThatThrownBy(() -> order.updateStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("newStatus can't be null");
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELLED"})
        void shouldAllowValidStatusTransitionsFromCreated(OrderStatus targetStatus) {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));

            order.updateStatus(targetStatus);
            assertThat(order.getStatus()).isEqualTo(targetStatus);
        }

        @ParameterizedTest
        @MethodSource("invalidStatusTransitions")
        void shouldThrowExceptionForInvalidStatusTransitions(
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String expectedMessage
        ) {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));

            // Set initial status if not CREATED
            if (oldStatus != OrderStatus.CREATED) {
                order.updateStatus(oldStatus);
            }

            assertThatThrownBy(() -> order.updateStatus(newStatus))
                .isInstanceOf(OrderStateTransitionNotAllowed.class)
                .hasMessage(expectedMessage);
        }
    }

    @Nested
    class OrderCalculations {

        static Stream<Arguments> orderCalculationTestData() {
            return Stream.of(
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", BigDecimal.TWO, BigDecimal.valueOf(10.0)),
                        new OrderItemData("product2", "Product 2", BigDecimal.ONE, BigDecimal.valueOf(15.0))
                    ),
                    BigDecimal.valueOf(35.0),
                    "Multiple items calculation: (2 * 10.0) + (1 * 15.0)"
                ),
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", BigDecimal.valueOf(1000), BigDecimal.valueOf(999.99))
                    ),
                    BigDecimal.valueOf(999990.00),
                    "Large numbers calculation: 1000 * 999.99"
                ),
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", BigDecimal.ONE, BigDecimal.valueOf(0.01))
                    ),
                    BigDecimal.valueOf(0.01),
                    "Small decimal calculation: 1 * 0.01"
                )
            );
        }

        @ParameterizedTest
        @MethodSource("orderCalculationTestData")
        void shouldCalculateTotalCorrectly(
            List<OrderItemData> items,
            BigDecimal expectedTotal,
            String testDescription
        ) {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));

            // When
            for (OrderItemData item : items) {
                order.addItem(
                    ProductSku.of(item.productSku()),
                    ProductTitle.of("product-123"),
                    Quantity.of(item.quantity()),
                    Money.of(Currency.getInstance("USD"), item.unitPrice())
                );
            }

            // Then
            Money expectedMoney = Money.of(Currency.getInstance("USD"), expectedTotal);
            assertThat(order.getTotal()).isEqualTo(expectedMoney);
        }

        @Test
        void shouldReturnNullTotalForEmptyOrder() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            assertThat(order.getTotal()).isNull();
        }

        record OrderItemData(String productSku, String productName, BigDecimal quantity, BigDecimal unitPrice) {}
    }

    @Nested
    class DomainEventManagement {

        @Test
        void shouldClearDomainEvents() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            order.addItem(ProductSku.of("product1"), ProductTitle.of("product-123"),
                Quantity.of(BigDecimal.ONE), Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(10.0)));
            order.raiseOrderCreatedEvents();
            order.updateStatus(OrderStatus.PAID);

            // When
            order.clearDomainEvents();

            // Then
            assertThat(order.getDomainEvents()).isEmpty();
        }

        @Test
        void shouldReturnImmutableEventList() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            List<DomainEvent> events = order.getDomainEvents();
            assertThatThrownBy(
                () -> events.add(
                    new DomainOrderCreatedEvent(OrderId.generate(), UserId.of("user123"), List.of(), Money.ZERO,
                        Money.ZERO, Money.ZERO, OrderAggregate.ZERO_VAT, Instant.now())))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldAccumulateEventsForMultipleOperations() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));
            order.addItem(ProductSku.of("product1"), ProductTitle.of("product-123"),
                Quantity.of(BigDecimal.ONE), Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(10.0)));
            order.raiseOrderCreatedEvents();
            order.updateStatus(OrderStatus.PAID);

            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(2);
            assertThat(events.getFirst()).isInstanceOf(DomainOrderCreatedEvent.class);
            assertThat(events.get(1)).isInstanceOf(DomainOrderStatusChangedEvent.class);
        }

        @Test
        void shouldRaiseSingleCreatedEventWithAllFinalOrderItems() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), UserId.of("user123"));

            order.addItem(
                ProductSku.of("product1"),
                ProductTitle.of("product-123"),
                Quantity.of(BigDecimal.ONE),
                Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(10.0))
            );
            order.addItem(
                ProductSku.of("product2"),
                ProductTitle.of("product-456"),
                Quantity.of(BigDecimal.TWO),
                Money.of(Currency.getInstance("USD"), BigDecimal.valueOf(15.0))
            );

            order.raiseOrderCreatedEvents();

            List<DomainOrderCreatedEvent> createdEvents = order.getDomainEvents().stream()
                .filter(DomainOrderCreatedEvent.class::isInstance)
                .map(DomainOrderCreatedEvent.class::cast)
                .toList();

            assertThat(createdEvents).hasSize(1);
            assertThat(createdEvents.getFirst().getItems()).hasSize(2);
        }
    }
}

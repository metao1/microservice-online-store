package com.metao.book.order.domain.model.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.order.domain.model.event.OrderItemAddedEvent;
import com.metao.book.order.domain.model.event.OrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
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
                Arguments.of(null, new CustomerId("customer123"), "Order ID cannot be null", "Null OrderId"),
                Arguments.of(OrderId.generate(), null, "Customer ID cannot be null", "Null CustomerId")
            );
        }

        @Test
        void shouldCreateOrderWithInitialState() {
            // Given
            OrderId orderId = OrderId.generate();
            CustomerId customerId = new CustomerId("customer123");

            // When
            OrderAggregate order = new OrderAggregate(orderId, customerId);

            // Then
            assertThat(order.getId()).isEqualTo(orderId);
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getItems()).isEmpty();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getCreatedAt()).isNotNull();
            assertThat(order.getUpdatedAt()).isNotNull();

            // Verify events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(DomainOrderCreatedEvent.class);

            DomainOrderCreatedEvent createdEvent = (DomainOrderCreatedEvent) events.getFirst();
            assertThat(createdEvent.getOrderId()).isEqualTo(orderId);
            assertThat(createdEvent.getCustomerId()).isEqualTo(customerId);
        }

        @ParameterizedTest
        @MethodSource("orderConstructorNullTestData")
        void shouldThrowExceptionForNullParameters(OrderId orderId, CustomerId customerId, String expectedMessage) {
            assertThatThrownBy(() -> new OrderAggregate(orderId, customerId))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
        }
    }

    @Nested
    class OrderItemManagement {

        @Test
        void shouldAddItemToOrder() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            order.clearDomainEvents(); // Clear initial creation event

            ProductId productId = new ProductId("product123");
            String productName = "Test Product";
            Quantity quantity = new Quantity(2);
            Money unitPrice = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(10.0));

            // When
            order.addItem(productId, productName, quantity, unitPrice);

            // Then
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getItems().getFirst().getProductId()).isEqualTo(productId);
            assertThat(order.getItems().getFirst().getQuantity()).isEqualTo(quantity);
            assertThat(order.getItems().getFirst().getUnitPrice()).isEqualTo(unitPrice);

            // Verify events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(OrderItemAddedEvent.class);

            OrderItemAddedEvent addedEvent = (OrderItemAddedEvent) events.getFirst();
            assertThat(addedEvent.getProductId()).isEqualTo(productId);
            assertThat(addedEvent.getProductName()).isEqualTo(productName);
            assertThat(addedEvent.getQuantity()).isEqualTo(quantity);
            assertThat(addedEvent.getUnitPrice()).isEqualTo(unitPrice);
        }

        @Test
        void shouldThrowExceptionForNullProductId() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            assertThatThrownBy(() -> order.addItem(null, "Test Product", new Quantity(1),
                new Money(Currency.getInstance("USD"), BigDecimal.valueOf(10.0))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product ID cannot be null");
        }

        @Test
        void shouldThrowExceptionForNegativeQuantity() {
            assertThatThrownBy(() -> new Quantity(-1))
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
                    "Cannot transition from CREATED to PENDING_PAYMENT"),
                Arguments.of(OrderStatus.CREATED, OrderStatus.PROCESSING,
                    "Cannot transition from CREATED to PROCESSING")
            );
        }

        @Test
        void shouldUpdateOrderStatus() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            order.clearDomainEvents(); // Clear initial creation event

            OrderStatus newStatus = OrderStatus.PAID;

            // When
            order.updateStatus(newStatus);

            // Then
            assertThat(order.getStatus()).isEqualTo(newStatus);

            // Verify events
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(OrderStatusChangedEvent.class);

            OrderStatusChangedEvent statusEvent = (OrderStatusChangedEvent) events.getFirst();
            assertThat(statusEvent.getOldStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(statusEvent.getNewStatus()).isEqualTo(newStatus);
        }

        @Test
        void shouldThrowExceptionForNullStatus() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            assertThatThrownBy(() -> order.updateStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New status cannot be null");
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELLED"})
        void shouldAllowValidStatusTransitionsFromCreated(OrderStatus targetStatus) {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));

            order.updateStatus(targetStatus);
            assertThat(order.getStatus()).isEqualTo(targetStatus);
        }

        @ParameterizedTest
        @MethodSource("invalidStatusTransitions")
        void shouldThrowExceptionForInvalidStatusTransitions(
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String expectedMessage
        ) {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));

            // Set initial status if not CREATED
            if (fromStatus != OrderStatus.CREATED) {
                order.updateStatus(fromStatus);
            }

            assertThatThrownBy(() -> order.updateStatus(toStatus))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(expectedMessage);
        }
    }

    @Nested
    class OrderCalculations {

        static Stream<Arguments> orderCalculationTestData() {
            return Stream.of(
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", 2, BigDecimal.valueOf(10.0)),
                        new OrderItemData("product2", "Product 2", 1, BigDecimal.valueOf(15.0))
                    ),
                    BigDecimal.valueOf(35.0),
                    "Multiple items calculation: (2 * 10.0) + (1 * 15.0)"
                ),
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", 1000, BigDecimal.valueOf(999.99))
                    ),
                    new BigDecimal("999990.00"),
                    "Large numbers calculation: 1000 * 999.99"
                ),
                Arguments.of(
                    List.of(
                        new OrderItemData("product1", "Product 1", 1, BigDecimal.valueOf(0.01))
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
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));

            // When
            for (OrderItemData item : items) {
                order.addItem(
                    new ProductId(item.productId()),
                    item.productName(),
                    new Quantity(item.quantity()),
                    new Money(Currency.getInstance("USD"), item.unitPrice())
                );
            }

            // Then
            Money expectedMoney = new Money(Currency.getInstance("USD"), expectedTotal);
            assertThat(order.getTotal()).isEqualTo(expectedMoney);
        }

        @Test
        void shouldReturnNullTotalForEmptyOrder() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            assertThat(order.getTotal()).isNull();
        }

        record OrderItemData(String productId, String productName, int quantity, BigDecimal unitPrice) {}
    }

    @Nested
    class DomainEventManagement {

        @Test
        void shouldClearDomainEvents() {
            // Given
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            order.addItem(new ProductId("product1"), "Product 1", new Quantity(1),
                new Money(Currency.getInstance("USD"), BigDecimal.valueOf(10.0)));
            order.updateStatus(OrderStatus.PAID);

            // When
            order.clearDomainEvents();

            // Then
            assertThat(order.getDomainEvents()).isEmpty();
        }

        @Test
        void shouldReturnImmutableEventList() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            List<DomainEvent> events = order.getDomainEvents();
            assertThatThrownBy(
                () -> events.add(new DomainOrderCreatedEvent(OrderId.generate(), new CustomerId("customer123"))))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldAccumulateEventsForMultipleOperations() {
            OrderAggregate order = new OrderAggregate(OrderId.generate(), new CustomerId("customer123"));
            order.addItem(new ProductId("product1"), "Product 1", new Quantity(1),
                new Money(Currency.getInstance("USD"), BigDecimal.valueOf(10.0)));
            order.updateStatus(OrderStatus.PAID);

            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(3); // Created + ItemAdded + StatusChanged
            assertThat(events.getFirst()).isInstanceOf(DomainOrderCreatedEvent.class);
            assertThat(events.get(1)).isInstanceOf(OrderItemAddedEvent.class);
            assertThat(events.get(2)).isInstanceOf(OrderStatusChangedEvent.class);
        }
    }
}
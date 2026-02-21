package com.metao.book.order.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrderItem Entity Tests")
class OrderItemTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    // ========== OrderItem Creation Tests ==========

    @Nested
    @DisplayName("OrderItem Creation Tests")
    class OrderItemCreationTests {

        @Test
        @DisplayName("should create order item with valid parameters")
        void createOrderItem_withValidParameters_shouldSucceed() {
            // GIVEN
            ProductSku productSku = ProductSku.of("PRODUCT1234");
            Quantity quantity = new Quantity(BigDecimal.valueOf(2));
            Money unitPrice = new Money(USD, BigDecimal.valueOf(10.00));

            // WHEN
            OrderItem orderItem = new OrderItem(productSku, quantity, unitPrice);

            // THEN
            assertThat(orderItem.getProductSku()).isEqualTo(productSku);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
            assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
        }

        @Test
        @DisplayName("should create order item with decimal quantity")
        void createOrderItem_withDecimalQuantity_shouldSucceed() {
            // GIVEN
            ProductSku productSku = ProductSku.of("PRODUCT456");
            Quantity quantity = new Quantity(new BigDecimal("2.5"));
            Money unitPrice = new Money(USD, BigDecimal.valueOf(8.00));

            // WHEN
            OrderItem orderItem = new OrderItem(productSku, quantity, unitPrice);

            // THEN
            assertThat(orderItem.getQuantity().getValue())
                .isEqualByComparingTo(new BigDecimal("2.5"));
        }

        @Test
        @DisplayName("should create order item with different currency")
        void createOrderItem_withDifferentCurrency_shouldSucceed() {
            // GIVEN
            ProductSku productSku = ProductSku.of("PRODUCT789");
            Quantity quantity = new Quantity(BigDecimal.ONE);
            Money unitPrice = new Money(EUR, BigDecimal.valueOf(15.00));

            // WHEN
            OrderItem orderItem = new OrderItem(productSku, quantity, unitPrice);

            // THEN
            assertThat(orderItem.getUnitPrice().currency()).isEqualTo(EUR);
        }
    }

    // ========== getTotalPrice Tests ==========

    @Nested
    @DisplayName("Total Price Calculation Tests")
    class TotalPriceCalculationTests {

        @Test
        @DisplayName("should calculate total price correctly for integer quantity")
        void getTotalPrice_withIntegerQuantity_shouldCalculateCorrectly() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-123"),
                new Quantity(BigDecimal.valueOf(3)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(30.00));
            assertThat(totalPrice.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("should calculate total price correctly for decimal quantity")
        void getTotalPrice_withDecimalQuantity_shouldCalculateCorrectly() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-456"),
                new Quantity(new BigDecimal("2.5")),
                new Money(USD, BigDecimal.valueOf(8.00))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(20.00));
        }

        @Test
        @DisplayName("should calculate total price with decimal unit price")
        void getTotalPrice_withDecimalUnitPrice_shouldCalculateCorrectly() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-789"),
                new Quantity(BigDecimal.valueOf(4)),
                new Money(USD, new BigDecimal("12.99"))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(new BigDecimal("51.96"));
        }

        @Test
        @DisplayName("should calculate total price for quantity of 1")
        void getTotalPrice_withQuantityOne_shouldEqualUnitPrice() {
            // GIVEN
            Money unitPrice = new Money(USD, BigDecimal.valueOf(25.00));
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-single"),
                new Quantity(BigDecimal.ONE),
                unitPrice
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice).isEqualTo(unitPrice);
        }

        @Test
        @DisplayName("should calculate total price with large quantity")
        void getTotalPrice_withLargeQuantity_shouldCalculateCorrectly() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-bulk"),
                new Quantity(new BigDecimal("1000")),
                new Money(USD, new BigDecimal("0.99"))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(new BigDecimal("990.00"));
        }

        @Test
        @DisplayName("should preserve currency in total price calculation")
        void getTotalPrice_shouldPreserveCurrency() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-eur"),
                new Quantity(BigDecimal.valueOf(2)),
                new Money(EUR, BigDecimal.valueOf(15.00))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.currency()).isEqualTo(EUR);
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(30.00));
        }
    }

    // ========== updateQuantity Tests ==========

    @Nested
    @DisplayName("Update Quantity Tests")
    class UpdateQuantityTests {

        @Test
        @DisplayName("should update quantity successfully")
        void updateQuantity_withNewQuantity_shouldUpdate() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-123"),
                new Quantity(BigDecimal.valueOf(2)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            Quantity newQuantity = new Quantity(BigDecimal.valueOf(5));

            // WHEN
            orderItem.updateQuantity(newQuantity);

            // THEN
            assertThat(orderItem.getQuantity()).isEqualTo(newQuantity);
            assertThat(orderItem.getQuantity().getValue())
                .isEqualByComparingTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("should update quantity to decimal value")
        void updateQuantity_toDecimalValue_shouldUpdate() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-456"),
                new Quantity(BigDecimal.valueOf(3)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            Quantity newQuantity = new Quantity(new BigDecimal("1.5"));

            // WHEN
            orderItem.updateQuantity(newQuantity);

            // THEN
            assertThat(orderItem.getQuantity().getValue())
                .isEqualByComparingTo(new BigDecimal("1.5"));
        }

        @Test
        @DisplayName("should update quantity and affect total price")
        void updateQuantity_shouldAffectTotalPrice() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-789"),
                new Quantity(BigDecimal.valueOf(2)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            Money originalTotal = orderItem.getTotalPrice();
            assertThat(originalTotal.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(20.00));

            // WHEN
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(4)));

            // THEN
            Money newTotal = orderItem.getTotalPrice();
            assertThat(newTotal.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(40.00));
        }

        @Test
        @DisplayName("should update quantity multiple times")
        void updateQuantity_multipleTimes_shouldRetainLastValue() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-multi"),
                new Quantity(BigDecimal.valueOf(1)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            // WHEN
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(2)));
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(3)));
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(5)));

            // THEN
            assertThat(orderItem.getQuantity().getValue())
                .isEqualByComparingTo(BigDecimal.valueOf(5));
        }
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle very small unit price")
        void shouldHandleVerySmallUnitPrice() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-penny"),
                new Quantity(BigDecimal.valueOf(100)),
                new Money(USD, new BigDecimal("0.01"))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("should handle very large total price")
        void shouldHandleVeryLargeTotalPrice() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-expensive"),
                new Quantity(new BigDecimal("1000")),
                new Money(USD, new BigDecimal("999.99"))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(new BigDecimal("999990.00"));
        }

        @Test
        @DisplayName("should handle precise decimal calculations")
        void shouldHandlePreciseDecimalCalculations() {
            // GIVEN
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-precise"),
                new Quantity(new BigDecimal("3.33")),
                new Money(USD, new BigDecimal("7.77"))
            );

            // WHEN
            Money totalPrice = orderItem.getTotalPrice();

            // THEN
            assertThat(totalPrice.fixedPointAmount())
                .isEqualByComparingTo(new BigDecimal("25.8741"));
        }

        @Test
        @DisplayName("should not modify unit price when quantity changes")
        void updateQuantity_shouldNotModifyUnitPrice() {
            // GIVEN
            Money unitPrice = new Money(USD, BigDecimal.valueOf(10.00));
            OrderItem orderItem = new OrderItem(
                new ProductSku("product-immutable"),
                new Quantity(BigDecimal.valueOf(2)),
                unitPrice
            );

            // WHEN
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(5)));

            // THEN
            assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
            assertThat(orderItem.getUnitPrice().fixedPointAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(10.00));
        }

        @Test
        @DisplayName("should maintain product ID through quantity updates")
        void updateQuantity_shouldMaintainProductId() {
            // GIVEN
            ProductSku productSku = ProductSku.of("PRODUCTSTB");
            OrderItem orderItem = new OrderItem(
                productSku,
                new Quantity(BigDecimal.valueOf(2)),
                new Money(USD, BigDecimal.valueOf(10.00))
            );

            // WHEN
            orderItem.updateQuantity(new Quantity(BigDecimal.valueOf(10)));

            // THEN
            assertThat(orderItem.getProductSku()).isEqualTo(productSku);
        }
    }
}

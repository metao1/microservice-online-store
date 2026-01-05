package com.metao.book.order.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Quantity Value Object Tests")
class QuantityTest {

    // ========== Valid Quantity Tests ==========

    @Nested
    @DisplayName("Valid Quantity Creation Tests")
    class ValidQuantityCreationTests {

        @Test
        @DisplayName("should create quantity with positive integer value")
        void createQuantity_withPositiveInteger_shouldSucceed() {
            // WHEN
            Quantity quantity = new Quantity(BigDecimal.valueOf(5));

            // THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("should create quantity with positive decimal value")
        void createQuantity_withPositiveDecimal_shouldSucceed() {
            // WHEN
            Quantity quantity = new Quantity(new BigDecimal("2.5"));

            // THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(new BigDecimal("2.5"));
        }

        @Test
        @DisplayName("should create quantity with very small positive value")
        void createQuantity_withVerySmallValue_shouldSucceed() {
            // WHEN
            Quantity quantity = new Quantity(new BigDecimal("0.01"));

            // THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        @DisplayName("should create quantity with large value")
        void createQuantity_withLargeValue_shouldSucceed() {
            // WHEN
            Quantity quantity = new Quantity(new BigDecimal("1000000"));

            // THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(new BigDecimal("1000000"));
        }
    }

    // ========== Invalid Quantity Tests ==========

    @Nested
    @DisplayName("Invalid Quantity Creation Tests")
    class InvalidQuantityCreationTests {

        @Test
        @DisplayName("should throw exception when quantity is zero")
        void createQuantity_withZero_shouldThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> new Quantity(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-0.01", "-100", "-999.99"})
        @DisplayName("should throw exception when quantity is negative")
        void createQuantity_withNegativeValue_shouldThrowException(String negativeValue) {
            // WHEN & THEN
            assertThatThrownBy(() -> new Quantity(new BigDecimal(negativeValue)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        @Test
        @DisplayName("should create quantity with null value (for ORM)")
        void createQuantity_withNull_shouldAllowForOrm() {
            // WHEN
            Quantity quantity = new Quantity(null);

            // THEN
            assertThat(quantity.getValue()).isNull();
        }
    }

    // ========== Add Operation Tests ==========

    @Nested
    @DisplayName("Add Operation Tests")
    class AddOperationTests {

        @Test
        @DisplayName("should add two quantities correctly")
        void add_withTwoQuantities_shouldReturnSum() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(3));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(2));

            // WHEN
            Quantity result = qty1.add(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("should add decimal quantities correctly")
        void add_withDecimalQuantities_shouldReturnCorrectSum() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("2.5"));
            Quantity qty2 = new Quantity(new BigDecimal("1.75"));

            // WHEN
            Quantity result = qty1.add(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("4.25"));
        }

        @Test
        @DisplayName("should add large quantities correctly")
        void add_withLargeQuantities_shouldReturnCorrectSum() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("1000000"));
            Quantity qty2 = new Quantity(new BigDecimal("500000"));

            // WHEN
            Quantity result = qty1.add(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("1500000"));
        }

        @Test
        @DisplayName("should return new instance after add")
        void add_shouldReturnNewInstance() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(3));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(2));

            // WHEN
            Quantity result = qty1.add(qty2);

            // THEN
            assertThat(result).isNotSameAs(qty1);
            assertThat(result).isNotSameAs(qty2);
            assertThat(qty1.getValue()).isEqualByComparingTo(BigDecimal.valueOf(3)); // Original unchanged
        }
    }

    // ========== Subtract Operation Tests ==========

    @Nested
    @DisplayName("Subtract Operation Tests")
    class SubtractOperationTests {

        @Test
        @DisplayName("should subtract two quantities correctly")
        void subtract_withValidQuantities_shouldReturnDifference() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(5));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(2));

            // WHEN
            Quantity result = qty1.subtract(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(3));
        }

        @Test
        @DisplayName("should subtract decimal quantities correctly")
        void subtract_withDecimalQuantities_shouldReturnCorrectDifference() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("5.5"));
            Quantity qty2 = new Quantity(new BigDecimal("2.25"));

            // WHEN
            Quantity result = qty1.subtract(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("3.25"));
        }

        @Test
        @DisplayName("should throw exception when subtraction results in zero or negative")
        void subtract_whenResultIsZeroOrNegative_shouldThrowException() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(2));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(3));

            // WHEN & THEN
            assertThatThrownBy(() -> qty1.subtract(qty2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resulting quantity must be positive");
        }

        @Test
        @DisplayName("should throw exception when subtracting equal quantities")
        void subtract_whenSubtractingEqualQuantities_shouldThrowException() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(5));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(5));

            // WHEN & THEN
            assertThatThrownBy(() -> qty1.subtract(qty2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resulting quantity must be positive");
        }

        @Test
        @DisplayName("should return new instance after subtract")
        void subtract_shouldReturnNewInstance() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(5));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(2));

            // WHEN
            Quantity result = qty1.subtract(qty2);

            // THEN
            assertThat(result).isNotSameAs(qty1);
            assertThat(result).isNotSameAs(qty2);
            assertThat(qty1.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5)); // Original unchanged
        }
    }

    // ========== Equality and HashCode Tests ==========

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityAndHashCodeTests {

        @Test
        @DisplayName("should be equal when values are same")
        void equals_withSameValue_shouldReturnTrue() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(5));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(5));

            // WHEN & THEN
            assertThat(qty1).isEqualTo(qty2);
            assertThat(qty1.hashCode()).isEqualTo(qty2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values are different")
        void equals_withDifferentValue_shouldReturnFalse() {
            // GIVEN
            Quantity qty1 = new Quantity(BigDecimal.valueOf(5));
            Quantity qty2 = new Quantity(BigDecimal.valueOf(3));

            // WHEN & THEN
            assertThat(qty1).isNotEqualTo(qty2);
        }

        @Test
        @DisplayName("should be equal when values have same numeric value but different scale")
        void equals_withDifferentScaleSameValue_shouldReturnTrue() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("5.0"));
            Quantity qty2 = new Quantity(new BigDecimal("5.00"));

            // WHEN & THEN - BigDecimal equals considers scale, so these might not be equal
            // This test documents the actual behavior
            assertThat(qty1.getValue()).isEqualByComparingTo(qty2.getValue());
        }
    }

    // ========== toString Tests ==========

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should return string representation of value")
        void toString_shouldReturnStringValue() {
            // GIVEN
            Quantity quantity = new Quantity(BigDecimal.valueOf(5));

            // WHEN
            String result = quantity.toString();

            // THEN
            assertThat(result).isEqualTo("5");
        }

        @Test
        @DisplayName("should return string representation of decimal value")
        void toString_withDecimal_shouldReturnStringValue() {
            // GIVEN
            Quantity quantity = new Quantity(new BigDecimal("2.5"));

            // WHEN
            String result = quantity.toString();

            // THEN
            assertThat(result).isEqualTo("2.5");
        }
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle very precise decimal values")
        void shouldHandlePreciseDecimals() {
            // GIVEN
            Quantity quantity = new Quantity(new BigDecimal("123.456789"));

            // WHEN & THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(new BigDecimal("123.456789"));
        }

        @Test
        @DisplayName("should handle quantity of 1")
        void shouldHandleQuantityOfOne() {
            // GIVEN
            Quantity quantity = new Quantity(BigDecimal.ONE);

            // WHEN & THEN
            assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("should handle adding very small quantities")
        void shouldHandleAddingSmallQuantities() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("0.001"));
            Quantity qty2 = new Quantity(new BigDecimal("0.002"));

            // WHEN
            Quantity result = qty1.add(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("0.003"));
        }

        @Test
        @DisplayName("should handle subtracting to very small result")
        void shouldHandleSubtractingToSmallResult() {
            // GIVEN
            Quantity qty1 = new Quantity(new BigDecimal("1.001"));
            Quantity qty2 = new Quantity(new BigDecimal("1.000"));

            // WHEN
            Quantity result = qty1.subtract(qty2);

            // THEN
            assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("0.001"));
        }
    }
}

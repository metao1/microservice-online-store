package com.metao.book.order.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

    // ========== Valid ProductId Tests ==========

    @Nested
    @DisplayName("Valid ProductId Creation Tests")
    class ValidProductIdCreationTests {

        @Test
        @DisplayName("should create product ID with valid value")
        void createProductId_withValidValue_shouldSucceed() {
            // GIVEN
            String value = "product-123";

            // WHEN
            ProductId productId = new ProductId(value);

            // THEN
            assertThat(productId.getValue()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(strings = {"P123", "product-abc-def", "SKU001", "1234567890"})
        @DisplayName("should accept various valid product ID formats")
        void createProductId_withVariousFormats_shouldSucceed(String value) {
            // WHEN
            ProductId productId = new ProductId(value);

            // THEN
            assertThat(productId.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("should create product ID with default constructor for Hibernate")
        void createProductId_withDefaultConstructor_shouldSucceed() {
            // WHEN
            ProductId productId = new ProductId();

            // THEN
            assertThat(productId.getValue()).isNull();
        }
    }

    // ========== Invalid ProductId Tests ==========

    @Nested
    @DisplayName("Invalid ProductId Creation Tests")
    class InvalidProductIdCreationTests {

        @Test
        @DisplayName("should throw exception when product ID is null")
        void createProductId_withNull_shouldThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
        }

        @Test
        @DisplayName("should throw exception when product ID is empty")
        void createProductId_withEmptyString_shouldThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> new ProductId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
        }

        @Test
        @DisplayName("should throw exception when product ID is blank")
        void createProductId_withBlankString_shouldThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> new ProductId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
        }

        @Test
        @DisplayName("should throw exception when product ID is only tabs")
        void createProductId_withTabs_shouldThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> new ProductId("\t\t"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
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
            String value = "product-123";
            ProductId id1 = new ProductId(value);
            ProductId id2 = new ProductId(value);

            // WHEN & THEN
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values are different")
        void equals_withDifferentValue_shouldReturnFalse() {
            // GIVEN
            ProductId id1 = new ProductId("product-123");
            ProductId id2 = new ProductId("product-456");

            // WHEN & THEN
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    // ========== toString Tests ==========

    @Test
    @DisplayName("toString should return value")
    void toString_shouldReturnValue() {
        // GIVEN
        String value = "product-123";
        ProductId productId = new ProductId(value);

        // WHEN
        String result = productId.toString();

        // THEN
        assertThat(result).isEqualTo(value);
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle very long product IDs")
        void createProductId_withLongValue_shouldSucceed() {
            // GIVEN
            String longValue = "P" + "X".repeat(1000);

            // WHEN
            ProductId productId = new ProductId(longValue);

            // THEN
            assertThat(productId.getValue()).isEqualTo(longValue);
        }

        @Test
        @DisplayName("should handle product IDs with special characters")
        void createProductId_withSpecialCharacters_shouldSucceed() {
            // GIVEN
            String valueWithSpecialChars = "prod-123_ABC-xyz.v2";

            // WHEN
            ProductId productId = new ProductId(valueWithSpecialChars);

            // THEN
            assertThat(productId.getValue()).isEqualTo(valueWithSpecialChars);
        }

        @Test
        @DisplayName("should accept product ID with leading/trailing whitespace but validate trimmed value")
        void createProductId_withLeadingTrailingWhitespace_shouldAcceptButStoreOriginal() {
            // WHEN
            ProductId productId = new ProductId("  product-123  ");

            // THEN
            // Validation trims, but stores original value
            assertThat(productId.getValue()).isEqualTo("  product-123  ");
        }
    }
}

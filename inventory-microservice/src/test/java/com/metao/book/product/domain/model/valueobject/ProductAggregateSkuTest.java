package com.metao.book.product.domain.model.valueobject;

import static com.metao.book.product.infrastructure.util.ProductConstant.SKU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.shared.domain.product.ProductSku;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProductSku Value Object Tests")
class ProductAggregateSkuTest {

    // ========== Valid SKU Tests ==========
    @Test
    @DisplayName("should accept non-empty SKUs")
    void testCreateProductSku_withValidSkus() {
        assertThat(SKU).isNotNull();
        assertThat(SKU.value()).isEqualTo("0594287995");
    }

    @Test
    @DisplayName("generated SKUs should be unique")
    void testGenerateProductSku_uniqueness() {
        // WHEN
        ProductSku sku1 = ProductSku.generate();
        ProductSku sku2 = ProductSku.generate();

        // THEN
        assertThat(sku1).isNotEqualTo(sku2);
        assertThat(sku1.value()).isNotEqualTo(sku2.value());
    }

    // ========== Invalid SKU Tests ==========

    @Test
    @DisplayName("should throw exception when SKU is null")
    void testCreateProductSku_whenNull_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ProductSku.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when SKU is empty")
    void testCreateProductSku_whenEmpty_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ProductSku.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ProductId cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when SKU is only whitespace")
    void testCreateProductSku_whenWhitespace_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ProductSku.of("          "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ProductId cannot be null or empty");
    }

    // ========== Trim Tests ==========

    @Test
    @DisplayName("should accept SKU without extra whitespace")
    void testCreateProductSku_withoutWhitespace() {
        // GIVEN
        String sku = "0594287995";

        // WHEN
        ProductSku productSku = ProductSku.of(sku);

        // THEN
        assertThat(productSku.value()).isEqualTo(sku);
        assertThat(productSku.toString()).isEqualTo(sku);
    }

    @Test
    @DisplayName("should trim whitespace around SKU")
    void testCreateProductSku_trimsWhitespace() {
        String skuWithWhitespace = "  ABC123   ";
        ProductSku productSku = ProductSku.of(skuWithWhitespace);
        assertThat(productSku.value()).isEqualTo("ABC123");
    }

    // ========== Equality and HashCode Tests ==========

    @Test
    @DisplayName("should be equal when SKU values are the same")
    void testProductSkuEquality_withSameValue() {
        String skuValue = "ABC123";
        ProductSku sku1 = ProductSku.of(skuValue);
        ProductSku sku2 = ProductSku.of(skuValue);
        assertThat(sku1).isEqualTo(sku2).hasSameHashCodeAs(sku2);
    }

    @Test
    @DisplayName("should not be equal when SKU values are different")
    void testProductSkuInequality_withDifferentValues() {
        // GIVEN
        ProductSku sku1 = ProductSku.of("0594287995");
        ProductSku sku2 = ProductSku.of("0594287996");

        // WHEN & THEN
        assertThat(sku1)
            .isNotEqualTo(sku2)
            .doesNotHaveSameHashCodeAs(sku2);
    }

    // ========== toString Tests ==========

    @Test
    @DisplayName("should return SKU value in toString")
    void testToString() {
        // GIVEN
        String skuValue = "TEST000001";
        ProductSku productSku = ProductSku.of(skuValue);

        // WHEN
        String stringValue = productSku.toString();

        // THEN
        assertThat(stringValue).isEqualTo(skuValue);
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("should accept SKU with all digits")
    void testCreateProductSku_allDigits() {
        // GIVEN
        String allDigits = "1234567890";

        // WHEN
        ProductSku productSku = ProductSku.of(allDigits);

        // THEN
        assertThat(productSku.value()).isEqualTo(allDigits);
    }

    @Test
    @DisplayName("should accept SKU with all letters")
    void testCreateProductSku_allLetters() {
        // GIVEN
        String allLetters = "ABCDEFGHIJ";

        // WHEN
        ProductSku productSku = ProductSku.of(allLetters);

        // THEN
        assertThat(productSku.value()).isEqualTo(allLetters);
    }

    @Test
    @DisplayName("should accept SKU with mixed case")
    void testCreateProductSku_mixedCase() {
        // GIVEN
        String mixedCase = "AbCdEfGhIj";

        // WHEN
        ProductSku productSku = ProductSku.of(mixedCase);

        // THEN
        assertThat(productSku.value()).isEqualTo(mixedCase);
    }

    @Test
    @DisplayName("should accept SKU with alphanumeric characters")
    void testCreateProductSku_alphanumeric() {
        // GIVEN
        String alphanumeric = "A1B2C3D4E5";

        // WHEN
        ProductSku productSku = ProductSku.of(alphanumeric);

        // THEN
        assertThat(productSku.value()).isEqualTo(alphanumeric);
    }
}

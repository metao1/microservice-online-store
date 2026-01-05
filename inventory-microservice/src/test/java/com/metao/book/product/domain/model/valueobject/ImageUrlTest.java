package com.metao.book.product.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ImageUrl Value Object Tests")
class ImageUrlTest {

    // ========== Valid URL Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com/image.jpg",
        "http://example.com/image.png",
        "https://example.com/image.gif",
        "https://example.com/web-dev.jpg",
        "https://example.com/my-image-file.PNG",
        "https://example.com/spring-book.GIF",
        "https://cdn.example.com/products/image123.jpg",
        "http://sub.domain.example.com/path/to/image.png"
    })
    @DisplayName("should accept valid HTTP(S) URLs")
    void testCreateImageUrl_withValidUrls(String url) {
        // WHEN
        ImageUrl imageUrl = ImageUrl.of(url);

        // THEN
        assertThat(imageUrl).isNotNull();
        assertThat(imageUrl.getValue()).isEqualTo(url);
    }

    @Test
    @DisplayName("should accept URLs with hyphens")
    void testCreateImageUrl_withHyphens() {
        // GIVEN
        String urlWithHyphens = "https://example.com/web-dev-tutorial-2024.jpg";

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(urlWithHyphens);

        // THEN
        assertThat(imageUrl.getValue()).isEqualTo(urlWithHyphens);
    }

    @Test
    @DisplayName("should accept uppercase file extensions")
    void testCreateImageUrl_withUppercaseExtensions() {
        // GIVEN
        String urlWithUppercase = "https://example.com/image.JPG";

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(urlWithUppercase);

        // THEN
        assertThat(imageUrl.getValue()).isEqualTo(urlWithUppercase);
    }

    // ========== Invalid URL Tests ==========

    @Test
    @DisplayName("should throw exception when URL is null")
    void testCreateImageUrl_whenNull_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when URL is empty")
    void testCreateImageUrl_whenEmpty_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when URL is only whitespace")
    void testCreateImageUrl_whenWhitespace_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image URL cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ftp://example.com/image.jpg",
        "//example.com/image.jpg",
        "example.com/image.jpg",
        "www.example.com/image.jpg"
    })
    @DisplayName("should throw exception when URL doesn't start with http(s)://")
    void testCreateImageUrl_withoutHttpProtocol_shouldThrowException(String url) {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com/image.bmp",
        "https://example.com/image.jpeg",
        "https://example.com/image.webp",
        "https://example.com/image.svg",
        "https://example.com/image.pdf"
    })
    @DisplayName("should throw exception when URL has unsupported file extension")
    void testCreateImageUrl_withUnsupportedExtension_shouldThrowException(String url) {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    @Test
    @DisplayName("should throw exception when URL exceeds 255 characters")
    void testCreateImageUrl_whenTooLong_shouldThrowException() {
        // GIVEN
        String longUrl = "https://example.com/" + "a".repeat(300) + ".jpg";

        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of(longUrl))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image URL cannot exceed 255 characters");
    }

    @Test
    @DisplayName("should throw exception when URL has no file extension")
    void testCreateImageUrl_withoutExtension_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> ImageUrl.of("https://example.com/image"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    // ========== Trim and Normalization Tests ==========

    @Test
    @DisplayName("should handle URLs without extra whitespace")
    void testCreateImageUrl_withoutWhitespace() {
        // GIVEN
        String url = "https://example.com/image.jpg";

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(url);

        // THEN
        assertThat(imageUrl.getValue()).isEqualTo(url);
        assertThat(imageUrl.toString()).isEqualTo(url);
    }

    // ========== Equality and HashCode Tests ==========

    @Test
    @DisplayName("should be equal when URLs are the same")
    void testImageUrlEquality_withSameUrl() {
        // GIVEN
        String url = "https://example.com/image.jpg";
        ImageUrl imageUrl1 = ImageUrl.of(url);
        ImageUrl imageUrl2 = ImageUrl.of(url);

        // WHEN & THEN
        assertThat(imageUrl1)
            .isEqualTo(imageUrl2)
            .hasSameHashCodeAs(imageUrl2);
    }

    @Test
    @DisplayName("should not be equal when URLs are different")
    void testImageUrlInequality_withDifferentUrls() {
        // GIVEN
        ImageUrl imageUrl1 = ImageUrl.of("https://example.com/image1.jpg");
        ImageUrl imageUrl2 = ImageUrl.of("https://example.com/image2.jpg");

        // WHEN & THEN
        assertThat(imageUrl1)
            .isNotEqualTo(imageUrl2)
            .doesNotHaveSameHashCodeAs(imageUrl2);
    }

    // ========== toString Tests ==========

    @Test
    @DisplayName("should return URL value in toString")
    void testToString() {
        // GIVEN
        String url = "https://example.com/product.jpg";
        ImageUrl imageUrl = ImageUrl.of(url);

        // WHEN
        String stringValue = imageUrl.toString();

        // THEN
        assertThat(stringValue).isEqualTo(url);
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("should accept URL at exact 255 character limit")
    void testCreateImageUrl_atMaxLength() {
        // GIVEN
        String baseUrl = "https://example.com/";
        String fileName = "a".repeat(255 - baseUrl.length() - 4) + ".jpg"; // -4 for .jpg
        String maxLengthUrl = baseUrl + fileName;

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(maxLengthUrl);

        // THEN
        assertThat(imageUrl.getValue()).hasSize(255);
    }

    @Test
    @DisplayName("should accept URLs with numbers")
    void testCreateImageUrl_withNumbers() {
        // GIVEN
        String urlWithNumbers = "https://example.com/image123456.jpg";

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(urlWithNumbers);

        // THEN
        assertThat(imageUrl.getValue()).contains("123456");
    }

    @Test
    @DisplayName("should accept URLs with underscores")
    void testCreateImageUrl_withUnderscores() {
        // GIVEN
        String urlWithUnderscores = "https://example.com/product_image_main.png";

        // WHEN
        ImageUrl imageUrl = ImageUrl.of(urlWithUnderscores);

        // THEN
        assertThat(imageUrl.getValue()).contains("_");
    }
}

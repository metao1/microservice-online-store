package com.metao.book.product.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ImageUrl Value Object Tests")
class ImageUrlTest {

    @ParameterizedTest
    @MethodSource("validUrls")
    @DisplayName("should accept valid HTTPS URLs")
    void testCreateImageUrl_withValidUrls(String url) {
        ImageUrl imageUrl = ImageUrl.of(url);

        assertThat(imageUrl).isNotNull();
        assertThat(imageUrl.getValue()).isEqualTo(url);
        assertThat(imageUrl.toString()).isEqualTo(url);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("should throw exception when URL is blank")
    void testCreateImageUrl_whenBlank_shouldThrowException(String url) {
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when URL is null")
    void testCreateImageUrl_whenNull_shouldThrowException() {
        assertThatThrownBy(() -> ImageUrl.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidProtocolUrls")
    @DisplayName("should throw exception when URL doesn't start with https://")
    void testCreateImageUrl_withoutHttpProtocol_shouldThrowException(String url) {
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    @ParameterizedTest
    @MethodSource("invalidExtensionUrls")
    @DisplayName("should throw exception when URL has unsupported file extension")
    void testCreateImageUrl_withUnsupportedExtension_shouldThrowException(String url) {
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    @ParameterizedTest
    @MethodSource("invalidFormatUrls")
    @DisplayName("should throw exception for invalid URL format")
    void testCreateImageUrl_withInvalidFormat_shouldThrowException(String url) {
        assertThatThrownBy(() -> ImageUrl.of(url))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image URL format");
    }

    @Test
    @DisplayName("should throw exception when URL exceeds 255 characters")
    void testCreateImageUrl_whenTooLong_shouldThrowException() {
        String longUrl = "https://example.com/" + "a".repeat(300) + ".jpg";

        assertThatThrownBy(() -> ImageUrl.of(longUrl))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image URL cannot exceed 255 characters");
    }

    @Test
    @DisplayName("should accept URL at exact 255 character limit")
    void testCreateImageUrl_atMaxLength() {
        String baseUrl = "https://example.com/";
        String fileName = "a".repeat(255 - baseUrl.length() - 4) + ".jpg";
        String maxLengthUrl = baseUrl + fileName;

        ImageUrl imageUrl = ImageUrl.of(maxLengthUrl);

        assertThat(imageUrl.getValue()).hasSize(255);
    }

    @ParameterizedTest
    @MethodSource("equalPairs")
    @DisplayName("should have expected equality")
    void testEquality(String first, String second, boolean expectedEqual) {
        ImageUrl imageUrl1 = ImageUrl.of(first);
        ImageUrl imageUrl2 = ImageUrl.of(second);

        if (expectedEqual) {
            assertThat(imageUrl1).isEqualTo(imageUrl2).hasSameHashCodeAs(imageUrl2);
        } else {
            assertThat(imageUrl1).isNotEqualTo(imageUrl2).doesNotHaveSameHashCodeAs(imageUrl2);
        }
    }

    private static Stream<String> validUrls() {
        return Stream.of(
            "https://example.com/image.jpg",
            "https://example.com/image.gif",
            "https://example.com/web-dev.jpg",
            "https://example.com/my-image-file.PNG",
            "https://example.com/spring-book.GIF",
            "https://cdn.example.com/products/image123.jpg",
            "https://example.com/web-dev-tutorial-2024.jpg",
            "https://example.com/image123456.jpg",
            "https://example.com/product_image_main.png",
            "https://ecx.images-amazon.com/images/I/A1ps1%2B09TTL.jpg"
        );
    }

    private static Stream<String> invalidProtocolUrls() {
        return Stream.of(
            "http://example.com/image.jpg",
            "http://sub.domain.example.com/path/to/image.png",
            "ftp://example.com/image.jpg",
            "//example.com/image.jpg",
            "example.com/image.jpg",
            "www.example.com/image.jpg"
        );
    }

    private static Stream<String> invalidExtensionUrls() {
        return Stream.of(
            "https://example.com/image.bmp",
            "https://example.com/image.jpeg",
            "https://example.com/image.webp",
            "https://example.com/image.svg",
            "https://example.com/image.pdf"
        );
    }

    private static Stream<String> invalidFormatUrls() {
        return Stream.of(
            "https://example.com/image",
            "https://example.com/image.jpg?size=small",
            "https://example.com/image.jpg#fragment"
        );
    }

    private static Stream<Arguments> equalPairs() {
        return Stream.of(
            Arguments.of("https://example.com/image.jpg", "https://example.com/image.jpg", true),
            Arguments.of("https://example.com/image1.jpg", "https://example.com/image2.jpg", false)
        );
    }
}

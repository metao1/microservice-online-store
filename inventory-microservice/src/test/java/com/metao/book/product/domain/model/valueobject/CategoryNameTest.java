package com.metao.book.product.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CategoryName Value Object Tests")
class CategoryNameTest {

    // ========== Valid Category Name Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {
        "books",
        "electronics",
        "home & garden",
        "sports & outdoors",
        "toys & games",
        "health & beauty",
        "automotive & industrial",
        "technology",
        "food & beverage"
    })
    @DisplayName("should accept valid category names")
    void testCreateCategoryName_withValidNames(String name) {
        // WHEN
        CategoryName categoryName = CategoryName.of(name);

        // THEN
        assertThat(categoryName).isNotNull();
        assertThat(categoryName.value()).isEqualTo(name);
    }

    @Test
    @DisplayName("should accept single character category name")
    void testCreateCategoryName_singleCharacter() {
        // WHEN
        CategoryName categoryName = CategoryName.of("A");

        // THEN
        assertThat(categoryName.value()).isEqualTo("a");
    }

    @Test
    @DisplayName("should accept category name at max length (100 characters)")
    void testCreateCategoryName_atMaxLength() {
        // GIVEN
        String maxLengthName = "a".repeat(100);

        // WHEN
        CategoryName categoryName = CategoryName.of(maxLengthName);

        // THEN
        assertThat(categoryName.value()).hasSize(100);
    }

    // ========== Invalid Category Name Tests ==========

    @Test
    @DisplayName("should throw exception when category name is null")
    void testCreateCategoryName_whenNull_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> CategoryName.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when category name is empty")
    void testCreateCategoryName_whenEmpty_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> CategoryName.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category name cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when category name is only whitespace")
    void testCreateCategoryName_whenWhitespace_shouldThrowException() {
        // WHEN & THEN
        assertThatThrownBy(() -> CategoryName.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category name cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when category name exceeds 100 characters")
    void testCreateCategoryName_whenTooLong_shouldThrowException() {
        // GIVEN
        String tooLongName = "a".repeat(101);

        // WHEN & THEN
        assertThatThrownBy(() -> CategoryName.of(tooLongName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category name cannot exceed 100 characters");
    }

    // ========== Trim Tests ==========

    @Test
    @DisplayName("should trim leading and trailing whitespace")
    void testCreateCategoryName_trimsWhitespace() {
        // GIVEN
        String nameWithWhitespace = "  Books  ";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithWhitespace);

        // THEN
        assertThat(categoryName.value()).isEqualTo("books");
    }

    @Test
    @DisplayName("should preserve internal whitespace")
    void testCreateCategoryName_preservesInternalWhitespace() {
        // GIVEN
        String nameWithSpaces = "Home & Garden";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithSpaces);

        // THEN
        assertThat(categoryName.value()).isEqualTo("home & garden");
    }

    // ========== Equality Tests ==========

    @Test
    @DisplayName("should be equal when category names are the same")
    void testCategoryNameEquality_withSameName() {
        // GIVEN
        String name = "Electronics";
        CategoryName name1 = CategoryName.of(name);
        CategoryName name2 = CategoryName.of(name);

        // WHEN & THEN
        assertThat(name1).isEqualTo(name2);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when category names are different")
    void testCategoryNameInequality_withDifferentNames() {
        // GIVEN
        CategoryName name1 = CategoryName.of("books");
        CategoryName name2 = CategoryName.of("electronics");

        // WHEN & THEN
        assertThat(name1).isNotEqualTo(name2);
        assertThat(name1.hashCode()).isNotEqualTo(name2.hashCode());
    }

    // ========== Special Characters Tests ==========

    @Test
    @DisplayName("should accept category name with ampersand")
    void testCreateCategoryName_withAmpersand() {
        // GIVEN
        String nameWithAmpersand = "Arts & Crafts";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithAmpersand);

        // THEN
        assertThat(categoryName.value()).contains("arts & crafts");
    }

    @Test
    @DisplayName("should accept category name with parentheses")
    void testCreateCategoryName_withParentheses() {
        // GIVEN
        String nameWithParens = "Books (Fiction)";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithParens);

        // THEN
        assertThat(categoryName.value()).isEqualTo("books (fiction)");
    }

    @Test
    @DisplayName("should accept category name with hyphens")
    void testCreateCategoryName_withHyphens() {
        // GIVEN
        String nameWithHyphens = "Non-Fiction";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithHyphens);

        // THEN
        assertThat(categoryName.value()).contains("-");
    }

    @Test
    @DisplayName("should accept category name with numbers")
    void testCreateCategoryName_withNumbers() {
        // GIVEN
        String nameWithNumbers = "Web 3.0 Development";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithNumbers);

        // THEN
        assertThat(categoryName.value()).contains("3.0");
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("should accept category name with unicode characters")
    void testCreateCategoryName_withUnicode() {
        // GIVEN
        String unicodeName = "bücher"; // German for Books

        // WHEN
        CategoryName categoryName = CategoryName.of(unicodeName);

        // THEN
        assertThat(categoryName.value()).isEqualTo("bücher");
    }

    @Test
    @DisplayName("should handle category name with multiple spaces between words")
    void testCreateCategoryName_withMultipleSpaces() {
        // GIVEN
        String nameWithSpaces = "Books     and     More";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithSpaces);

        // THEN
        // Internal spaces are preserved
        assertThat(categoryName.value()).contains("     ");
    }

    @Test
    @DisplayName("should accept category name with apostrophe")
    void testCreateCategoryName_withApostrophe() {
        // GIVEN
        String nameWithApostrophe = "Children's Books";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithApostrophe);

        // THEN
        assertThat(categoryName.value()).contains("'");
    }

    @Test
    @DisplayName("should accept category name with forward slash")
    void testCreateCategoryName_withSlash() {
        // GIVEN
        String nameWithSlash = "Arts/Crafts";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithSlash);

        // THEN
        assertThat(categoryName.value()).contains("/");
    }

    @Test
    @DisplayName("should accept category name with comma")
    void testCreateCategoryName_withComma() {
        // GIVEN
        String nameWithComma = "Books, Movies & Music";

        // WHEN
        CategoryName categoryName = CategoryName.of(nameWithComma);

        // THEN
        assertThat(categoryName.value()).contains(",");
    }
}

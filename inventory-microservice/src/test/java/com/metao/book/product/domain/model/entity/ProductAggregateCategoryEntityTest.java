package com.metao.book.product.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductCategory Entity Tests")
class ProductAggregateCategoryEntityTest {

    @Test
    @DisplayName("should create category with factory method of(CategoryId, CategoryName)")
    void testCreateCategoryWithIdAndName() {
        // GIVEN
        String categoryIdValue = UUID.randomUUID().toString();
        CategoryId categoryId = CategoryId.of(categoryIdValue);
        CategoryName categoryName = CategoryName.of("Books");

        // WHEN
        ProductCategory category = ProductCategory.of(categoryId, categoryName);

        // THEN
        assertThat(category)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", categoryId)
            .hasFieldOrPropertyWithValue("name", categoryName);
    }

    @Test
    @DisplayName("should create category with factory method of(CategoryName)")
    void testCreateCategoryWithOnlyName() {
        // GIVEN
        CategoryName categoryName = CategoryName.of("Electronics");

        // WHEN
        ProductCategory category = ProductCategory.of(categoryName);

        // THEN
        assertThat(category)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", categoryName);

        // Category ID should be generated (non-null and unique)
        assertThat(category.getId())
            .isNotNull()
            .extracting(CategoryId::value)
            .isNotNull();
    }

    @Test
    @DisplayName("should have correct toString representation")
    void testToString() {
        // GIVEN
        CategoryName categoryName = CategoryName.of("Fiction");
        ProductCategory category = ProductCategory.of(categoryName);

        // WHEN
        String stringRepresentation = category.toString();

        // THEN
        assertThat(stringRepresentation)
            .isEqualTo(categoryName.toString())
            .contains("fiction");
    }

    @Test
    @DisplayName("categories with same ID and name should be equal")
    void testCategoryEquality_withSameIdAndName() {
        // GIVEN
        String categoryIdValue = UUID.randomUUID().toString();
        CategoryId categoryId = CategoryId.of(categoryIdValue);
        CategoryName categoryName = CategoryName.of("Books");

        ProductCategory category1 = ProductCategory.of(categoryId, categoryName);
        ProductCategory category2 = ProductCategory.of(categoryId, categoryName);

        // WHEN & THEN
        assertThat(category1)
            .isEqualTo(category2)
            .hasSameHashCodeAs(category2);
    }

    @Test
    @DisplayName("categories with different IDs should not be equal")
    void testCategoryInequality_withDifferentIds() {
        // GIVEN
        CategoryId categoryId1 = CategoryId.of(UUID.randomUUID().toString());
        CategoryId categoryId2 = CategoryId.of(UUID.randomUUID().toString());
        CategoryName categoryName = CategoryName.of("Books");

        ProductCategory category1 = ProductCategory.of(categoryId1, categoryName);
        ProductCategory category2 = ProductCategory.of(categoryId2, categoryName);

        // WHEN & THEN
        assertThat(category1)
            .isNotEqualTo(category2)
            .doesNotHaveSameHashCodeAs(category2);
    }

    @Test
    @DisplayName("categories with different names but same ID should not be equal")
    void testCategoryInequality_withDifferentNames() {
        // GIVEN
        String categoryIdValue = UUID.randomUUID().toString();
        CategoryId categoryId = CategoryId.of(categoryIdValue);
        CategoryName categoryName1 = CategoryName.of("Books");
        CategoryName categoryName2 = CategoryName.of("Electronics");

        ProductCategory category1 = ProductCategory.of(categoryId, categoryName1);
        ProductCategory category2 = ProductCategory.of(categoryId, categoryName2);

        // WHEN & THEN
        assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("category should not be equal to null")
    void testCategoryNotEqualToNull() {
        // GIVEN
        ProductCategory category = ProductCategory.of(CategoryName.of("Books"));

        // WHEN & THEN
        assertThat(category).isNotNull();
    }

    @Test
    @DisplayName("category should not be equal to object of different type")
    void testCategoryNotEqualToDifferentType() {
        // GIVEN
        ProductCategory category = ProductCategory.of(CategoryName.of("Books"));
        Object differentObject = new Object();

        // WHEN & THEN
        assertThat(category).isNotEqualTo(differentObject);
    }

    @Test
    @DisplayName("category name getter should return correct name")
    void testGetCategoryName() {
        // GIVEN
        CategoryName categoryName = CategoryName.of("Science Fiction");
        ProductCategory category = ProductCategory.of(categoryName);

        // WHEN
        CategoryName retrievedName = category.getName();

        // THEN
        assertThat(retrievedName)
            .isEqualTo(categoryName)
            .hasFieldOrPropertyWithValue("value", "science fiction");
    }

    @Test
    @DisplayName("category ID getter should return correct ID")
    void testGetCategoryId() {
        // GIVEN
        String categoryIdValue = UUID.randomUUID().toString();
        CategoryId categoryId = CategoryId.of(categoryIdValue);
        CategoryName categoryName = CategoryName.of("books");
        ProductCategory category = ProductCategory.of(categoryId, categoryName);

        // WHEN
        CategoryId retrievedId = category.getId();

        // THEN
        assertThat(retrievedId)
            .isEqualTo(categoryId)
            .hasFieldOrPropertyWithValue("value", categoryIdValue);
    }

    @Test
    @DisplayName("generated categories should have unique IDs")
    void testGeneratedCategoriesHaveUniqueIds() {
        // GIVEN
        CategoryName categoryName = CategoryName.of("books");

        // WHEN
        ProductCategory category1 = ProductCategory.of(categoryName);
        ProductCategory category2 = ProductCategory.of(categoryName);

        // THEN
        assertThat(category1.getId())
            .isNotEqualTo(category2.getId());

        assertThat(category1)
            .isNotEqualTo(category2);
    }

    @Test
    @DisplayName("should handle category names with various characters")
    void testCategoryNameWithVariousCharacters() {
        // GIVEN
        String complexName = "science & technology books (2024)";

        // WHEN
        ProductCategory category = ProductCategory.of(CategoryName.of(complexName));

        // THEN
        assertThat(category.getName())
            .isEqualTo(CategoryName.of(complexName))
            .hasFieldOrPropertyWithValue("value", complexName);
    }
}


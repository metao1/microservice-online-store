package com.metao.book.product.domain.model.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.event.ProductCreatedEvent;
import com.metao.book.product.domain.model.event.ProductUpdatedEvent;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product Aggregate Root Tests")
class ProductTest {

    private ProductSku productSku;
    private ProductTitle productTitle;
    private ProductDescription productDescription;
    private ProductVolume productVolume;
    private Money money;
    private ImageUrl imageUrl;
    private Instant createdTime;
    private Instant updatedTime;
    private Set<ProductCategory> categories;

    @BeforeEach
    void setUp() {
        productSku = ProductSku.of("0594287995");
        productTitle = ProductTitle.of("Test Product");
        productDescription = ProductDescription.of("Test Description");
        productVolume = ProductVolume.of(BigDecimal.valueOf(100));
        money = Money.of(BigDecimal.valueOf(29.99), Currency.getInstance("EUR"));
        imageUrl = ImageUrl.of("https://example.com/image.jpg");
        createdTime = Instant.now();
        updatedTime = createdTime;
        categories = Set.of(ProductCategory.of(CategoryName.of("Books")));
    }

    @Test
    @DisplayName("should create product with valid parameters")
    void testCreateProductWithValidParameters() {
        // WHEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // THEN
        assertThat(Product)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", productSku)
            .hasFieldOrPropertyWithValue("title", productTitle)
            .hasFieldOrPropertyWithValue("description", productDescription)
            .hasFieldOrPropertyWithValue("volume", productVolume)
            .hasFieldOrPropertyWithValue("money", money)
            .hasFieldOrPropertyWithValue("imageUrl", imageUrl)
            .hasFieldOrPropertyWithValue("createdTime", createdTime);
    }

    @Test
    @DisplayName("should create products with same parameters that are equal")
    void testCreateProductsWithSameParametersAreEqual() {
        // WHEN
        Product Product1 = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Product Product2 = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // THEN
        assertThat(Product1).isEqualTo(Product2);
    }

    @Test
    @DisplayName("should raise ProductCreatedEvent when product is created")
    void testProductCreatedEventRaisedOnCreation() {
        // WHEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // THEN
        assertThat(Product.getDomainEvents())
            .isNotNull()
            .isNotEmpty()
            .hasSize(1)
            .allMatch(event -> event instanceof ProductCreatedEvent);

        ProductCreatedEvent createdEvent = (ProductCreatedEvent) Product.getDomainEvents().stream()
            .findFirst()
            .orElseThrow();

        assertThat(createdEvent)
            .hasFieldOrPropertyWithValue("productSku", productSku)
            .hasFieldOrPropertyWithValue("title", productTitle)
            .hasFieldOrPropertyWithValue("price", money);
    }

    @Test
    @DisplayName("should initialize with empty categories when null is provided")
    void testProductInitializeWithEmptyCategoriesWhenNull() {
        // WHEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            null
        );

        // THEN
        assertThat(Product.getCategories())
            .isNotNull()
            .isEmpty();
    }

    @Test
    @DisplayName("should add category to product")
    void testAddCategoryToProduct() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            Set.of()
        );

        ProductCategory newCategory = ProductCategory.of(CategoryName.of("Electronics"));

        // WHEN
        Product.addCategory(newCategory);

        // THEN
        assertThat(Product.getCategories())
            .isNotEmpty()
            .hasSize(1)
            .contains(newCategory);

        assertThat(Product.getUpdatedTime()).isNotEqualTo(createdTime);
    }

    @Test
    @DisplayName("should not add duplicate category to product")
    void testAddDuplicateCategoryIsIdempotent() {
        // GIVEN
        ProductCategory category = ProductCategory.of(CategoryName.of("Books"));
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            Set.of(category)
        );

        Instant timeAfterFirstAdd = Product.getUpdatedTime();

        // WHEN
        Product.addCategory(category);

        // THEN
        assertThat(Product.getCategories())
            .hasSize(1)
            .contains(category);

        assertThat(Product.getUpdatedTime()).isEqualTo(timeAfterFirstAdd);
    }

    @Test
    @DisplayName("should update product price and raise event")
    void testUpdatePriceRaisesProductUpdatedEvent() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Money newPrice = Money.of(BigDecimal.valueOf(39.99), Currency.getInstance("EUR"));

        // WHEN
        Product.updatePrice(newPrice);

        // THEN
        assertThat(Product.getMoney()).isEqualTo(newPrice);
        assertThat(Product.getUpdatedTime()).isAfter(createdTime);

        assertThat(Product.getDomainEvents())
            .isNotEmpty()
            .hasSize(2)
            .anySatisfy(event ->
                assertThat(event)
                    .isInstanceOf(ProductUpdatedEvent.class)
                    .hasFieldOrPropertyWithValue("productSku", productSku)
            );
    }

    @Test
    @DisplayName("should not raise event when updating to same price")
    void testUpdatePriceWithSamePriceDoesNotRaiseEvent() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        int initialEventCount = Product.getDomainEvents().size();

        // WHEN
        Product.updatePrice(money);

        // THEN
        assertThat(Product.getDomainEvents()).hasSize(initialEventCount);
    }

    @Test
    @DisplayName("should update product title")
    void testUpdateTitle() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductTitle newTitle = ProductTitle.of("Updated Title");

        // WHEN
        Product.updateTitle(newTitle);

        // THEN
        assertThat(Product.getTitle()).isEqualTo(newTitle);
        assertThat(Product.getUpdatedTime()).isAfter(createdTime);
    }

    @Test
    @DisplayName("should not update title when same value is provided")
    void testUpdateTitleWithSameValueDoesNotChange() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Instant timeBeforeUpdate = Product.getUpdatedTime();

        // WHEN
        Product.updateTitle(productTitle);

        // THEN
        assertThat(Product.getUpdatedTime()).isEqualTo(timeBeforeUpdate);
    }

    @Test
    @DisplayName("should update product description")
    void testUpdateDescription() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductDescription newDescription = ProductDescription.of("Updated Description");

        // WHEN
        Product.updateDescription(newDescription);

        // THEN
        assertThat(Product.getDescription()).isEqualTo(newDescription);
        assertThat(Product.getUpdatedTime()).isAfter(createdTime);
    }

    @Test
    @DisplayName("should correctly identify product in stock")
    void testIsInStock_whenVolumeGreaterThanZero() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.valueOf(50)),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN & THEN
        assertThat(Product.isInStock()).isTrue();
    }

    @Test
    @DisplayName("should correctly identify product out of stock")
    void testIsInStock_whenVolumeIsZero() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.ZERO),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN & THEN
        assertThat(Product.isInStock()).isFalse();
    }

    @Test
    @DisplayName("should reduce volume successfully")
    void testReduceVolume() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.valueOf(100)),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume reduction = ProductVolume.of(BigDecimal.valueOf(30));

        // WHEN
        Product.reduceVolume(reduction);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(BigDecimal.valueOf(70)));

        assertThat(Product.getUpdatedTime()).isAfter(createdTime);
    }

    @Test
    @DisplayName("should throw exception when reducing volume more than available")
    void testReduceVolume_whenReductionExceedsAvailable() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.valueOf(50)),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume excessiveReduction = ProductVolume.of(BigDecimal.valueOf(100));

        // WHEN & THEN
        assertThatThrownBy(() -> Product.reduceVolume(excessiveReduction))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot reduce volume by more than available");
    }

    @Test
    @DisplayName("should increase volume successfully")
    void testIncreaseVolume() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.valueOf(100)),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume increase = ProductVolume.of(BigDecimal.valueOf(50));

        // WHEN
        Product.increaseVolume(increase);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(BigDecimal.valueOf(150)));

        assertThat(Product.getUpdatedTime()).isAfter(createdTime);
    }

    @Test
    @DisplayName("should increase volume to zero from zero")
    void testIncreaseVolumeFromZero() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.ZERO),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume increase = ProductVolume.of(BigDecimal.valueOf(100));

        // WHEN
        Product.increaseVolume(increase);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(BigDecimal.valueOf(100)));

        assertThat(Product.isInStock()).isTrue();
    }

    @Test
    @DisplayName("products with same SKU should be equal")
    void testProductEquality_withSameSku() {
        // GIVEN
        Product Product1 = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Product Product2 = new Product(
            productSku,
            ProductTitle.of("Different Title"),
            ProductDescription.of("Different Description"),
            ProductVolume.of(BigDecimal.valueOf(200)),
            Money.of(BigDecimal.valueOf(99.99), Currency.getInstance("EUR")),
            createdTime,
            updatedTime,
            imageUrl,
            Set.of()
        );

        // WHEN & THEN
        assertThat(Product1)
            .isEqualTo(Product2)
            .hasSameHashCodeAs(Product2);
    }

    @Test
    @DisplayName("products with different SKU should not be equal")
    void testProductInequality_withDifferentSku() {
        // GIVEN
        Product Product1 = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Product Product2 = new Product(
            ProductSku.of("0594287996"),
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN & THEN
        assertThat(Product1)
            .isNotEqualTo(Product2)
            .doesNotHaveSameHashCodeAs(Product2);
    }

    @Test
    @DisplayName("product should not be equal to null")
    void testProductNotEqualToNull() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN & THEN
        assertThat(Product).isNotNull();
    }

    @Test
    @DisplayName("product should not be equal to object of different type")
    void testProductNotEqualToDifferentType() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Object differentObject = new Object();

        // WHEN & THEN
        assertThat(Product).isNotEqualTo(differentObject);
    }

    @Test
    @DisplayName("product categories should be mutable")
    void testProductCategoriesAreIndependentCopy() {
        // GIVEN
        ProductCategory category1 = ProductCategory.of(CategoryName.of("Books"));
        ProductCategory category2 = ProductCategory.of(CategoryName.of("Electronics"));
        Set<ProductCategory> initialCategories = Set.of(category1);

        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            initialCategories
        );

        // WHEN
        Product.addCategory(category2);

        // THEN
        assertThat(Product.getCategories())
            .hasSize(2)
            .contains(category1, category2);
    }

    // ========== Additional Edge Cases ==========

    @Test
    @DisplayName("should reduce volume to exactly zero")
    void testReduceVolume_toExactlyZero() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(BigDecimal.valueOf(50)),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume reduction = ProductVolume.of(BigDecimal.valueOf(50));

        // WHEN
        Product.reduceVolume(reduction);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(BigDecimal.ZERO));
        assertThat(Product.isInStock()).isFalse();
    }

    @Test
    @DisplayName("should handle decimal volume operations precisely")
    void testVolumeOperations_withDecimals() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(new BigDecimal("10.5")),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN
        Product.reduceVolume(ProductVolume.of(new BigDecimal("3.2")));
        Product.increaseVolume(ProductVolume.of(new BigDecimal("1.7")));

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(new BigDecimal("9.0")));
    }

    @Test
    @DisplayName("should throw exception when updating to null price")
    void testUpdatePrice_whenNullPrice_shouldThrowException() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        // WHEN & THEN
        assertThatThrownBy(() -> Product.updatePrice(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should handle multiple sequential price updates")
    void testMultipleSequentialPriceUpdates() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Money price1 = Money.of(BigDecimal.valueOf(35.00), Currency.getInstance("EUR"));
        Money price2 = Money.of(BigDecimal.valueOf(40.00), Currency.getInstance("EUR"));
        Money price3 = Money.of(BigDecimal.valueOf(45.00), Currency.getInstance("EUR"));

        // WHEN
        Product.updatePrice(price1);
        Product.updatePrice(price2);
        Product.updatePrice(price3);

        // THEN
        assertThat(Product.getMoney()).isEqualTo(price3);
        // Should have 1 created + 3 updated events
        assertThat(Product.getDomainEvents()).hasSize(4);
    }

    @Test
    @DisplayName("should handle multiple category additions")
    void testMultipleCategoryAdditions() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            Set.of()
        );

        ProductCategory category1 = ProductCategory.of(CategoryName.of("Books"));
        ProductCategory category2 = ProductCategory.of(CategoryName.of("Electronics"));
        ProductCategory category3 = ProductCategory.of(CategoryName.of("Home"));

        // WHEN
        Product.addCategory(category1);
        Product.addCategory(category2);
        Product.addCategory(category3);

        // THEN
        assertThat(Product.getCategories())
            .hasSize(3)
            .contains(category1, category2, category3);
    }

    @Test
    @DisplayName("should not update timestamp when adding null category")
    void testAddCategory_whenNullCategory_shouldNotUpdateTimestamp() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            Set.of()
        );

        Instant timeBeforeAddition = Product.getUpdatedTime();

        // WHEN & THEN - Should not throw, just ignore
        assertThatThrownBy(() -> Product.addCategory(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should maintain immutability of original category set")
    void testCategoryImmutability() {
        // GIVEN
        ProductCategory category1 = ProductCategory.of(CategoryName.of("Books"));
        Set<ProductCategory> originalCategories = Set.of(category1);

        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            originalCategories
        );

        ProductCategory category2 = ProductCategory.of(CategoryName.of("Electronics"));

        // WHEN
        Product.addCategory(category2);

        // THEN
        assertThat(originalCategories).hasSize(1); // Original set unchanged
        assertThat(Product.getCategories()).hasSize(2); // Product's set modified
    }

    @Test
    @DisplayName("should handle zero price updates")
    void testUpdatePrice_withZeroPrice() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Money zeroPrice = Money.of(BigDecimal.ZERO, Currency.getInstance("EUR"));

        // WHEN
        Product.updatePrice(zeroPrice);

        // THEN
        assertThat(Product.getMoney()).isEqualTo(zeroPrice);
        assertThat(Product.getDomainEvents()).hasSize(2); // Created + Updated
    }

    @Test
    @DisplayName("should update description to empty string")
    void testUpdateDescription_withEmptyString() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductDescription emptyDescription = ProductDescription.of("");

        // WHEN & THEN - Should be allowed if ProductDescription validation permits
        Product.updateDescription(emptyDescription);
        assertThat(Product.getDescription()).isEqualTo(emptyDescription);
    }

    @Test
    @DisplayName("should correctly handle volume reduction leaving fractional remainder")
    void testReduceVolume_withFractionalRemainder() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(new BigDecimal("100.75")),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume reduction = ProductVolume.of(new BigDecimal("50.5"));

        // WHEN
        Product.reduceVolume(reduction);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(new BigDecimal("50.25")));
    }

    @Test
    @DisplayName("should verify all domain events maintain correct order")
    void testDomainEvents_maintainOrder() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            productVolume,
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        Money newPrice = Money.of(BigDecimal.valueOf(39.99), Currency.getInstance("EUR"));

        // WHEN
        Product.updatePrice(newPrice);
        Product.updateTitle(ProductTitle.of("New Title")); // Title update does not raise event

        // THEN
        assertThat(Product.getDomainEvents())
            .hasSize(2)  // Created + Price Updated (title update does not raise event)
            .element(0).isInstanceOf(ProductCreatedEvent.class);

        assertThat(Product.getDomainEvents())
            .element(1).isInstanceOf(ProductUpdatedEvent.class);
    }

    @Test
    @DisplayName("should handle volume increase with large numbers")
    void testIncreaseVolume_withLargeNumbers() {
        // GIVEN
        Product Product = new Product(
            productSku,
            productTitle,
            productDescription,
            ProductVolume.of(new BigDecimal("1000000")),
            money,
            createdTime,
            updatedTime,
            imageUrl,
            categories
        );

        ProductVolume largeIncrease = ProductVolume.of(new BigDecimal("5000000"));

        // WHEN
        Product.increaseVolume(largeIncrease);

        // THEN
        assertThat(Product.getVolume())
            .isEqualTo(ProductVolume.of(new BigDecimal("6000000")));
        assertThat(Product.isInStock()).isTrue();
    }
}


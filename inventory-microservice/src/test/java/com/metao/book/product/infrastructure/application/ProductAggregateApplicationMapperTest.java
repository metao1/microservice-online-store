package com.metao.book.product.infrastructure.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductApplicationMapper Tests")
class ProductAggregateApplicationMapperTest {

    private ProductApplicationMapper mapper;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        mapper = new ProductApplicationMapper();
        eurCurrency = Currency.getInstance("EUR");
    }

    // ========== toDomain Mapping Tests ==========

    @Nested
    @DisplayName("toDomain Mapping Tests")
    class ToDomainMappingTests {

        @Test
        @DisplayName("should map CreateProductDto to Product domain object")
        void toDomain_withValidDto_shouldMapToDomain() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000001",
                "Test Product",
                "Test Description",
                "https://example.com/image.jpg",
                BigDecimal.valueOf(29.99),
                eurCurrency,
                BigDecimal.valueOf(100),
                Set.of("Books", "Technology")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product).isNotNull();
            assertThat(product.getId().value()).isEqualTo("TEST000001");
            assertThat(product.getTitle().getValue()).isEqualTo("Test Product");
            assertThat(product.getDescription().getValue()).isEqualTo("Test Description");
            assertThat(product.getImageUrl().getValue()).isEqualTo("https://example.com/image.jpg");
            assertThat(product.getMoney().fixedPointAmount()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
            assertThat(product.getMoney().currency()).isEqualTo(eurCurrency);
            assertThat(product.getVolume().getValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(product.getCategories()).hasSize(2);
        }

        @Test
        @DisplayName("should map CreateProductDto with multiple categories")
        void toDomain_withMultipleCategories_shouldMapAllCategories() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000002",
                "Multi-Category Product",
                "Product with multiple categories",
                "https://example.com/multi.jpg",
                BigDecimal.valueOf(49.99),
                eurCurrency,
                BigDecimal.valueOf(50),
                Set.of("books", "technology", "programming", "education")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product.getCategories())
                .hasSize(4)
                .extracting(cat -> cat.getName().value())
                .containsExactlyInAnyOrder("books", "technology", "programming", "education");
        }

        @Test
        @DisplayName("should map CreateProductDto with empty categories")
        void toDomain_withEmptyCategories_shouldCreateProductWithNoCategories() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000003",
                "No Category Product",
                "Product without categories",
                "https://example.com/nocategory.jpg",
                BigDecimal.valueOf(19.99),
                eurCurrency,
                BigDecimal.valueOf(25),
                Set.of()
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product.getCategories()).isEmpty();
        }

        @Test
        @DisplayName("should preserve timestamps when mapping")
        void toDomain_shouldSetCreatedAndUpdatedTimesToSameValue() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000004",
                "Timestamp Product",
                "Product to test timestamps",
                "https://example.com/timestamp.jpg",
                BigDecimal.valueOf(39.99),
                eurCurrency,
                BigDecimal.valueOf(75),
                Set.of("Books")
            );

            Instant beforeMapping = Instant.now();

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            Instant afterMapping = Instant.now();

            // THEN
            assertThat(product.getCreatedTime())
                .isAfterOrEqualTo(beforeMapping)
                .isBeforeOrEqualTo(afterMapping);
            assertThat(product.getUpdatedTime()).isEqualTo(product.getCreatedTime());
        }

        @Test
        @DisplayName("should handle decimal values in price and volume")
        void toDomain_withDecimalValues_shouldPreservePrecision() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000005",
                "Decimal Product",
                "Product with decimal price and volume",
                "https://example.com/decimal.jpg",
                new BigDecimal("99.95"),
                eurCurrency,
                new BigDecimal("10.5"),
                Set.of("Books")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product.getMoney().fixedPointAmount()).isEqualByComparingTo(new BigDecimal("99.95"));
            assertThat(product.getVolume().getValue()).isEqualByComparingTo(new BigDecimal("10.5"));
        }
    }

    // ========== toDTO Mapping Tests ==========

    @Nested
    @DisplayName("toDTO Mapping Tests")
    class ToDTOMappingTests {

        @Test
        @DisplayName("should map Product domain object to ProductDTO")
        void toDTO_withValidProduct_shouldMapToDTO() {
            // GIVEN
            ProductSku sku = ProductSku.of("TEST000001");
            ProductTitle title = ProductTitle.of("Test Product");
            ProductDescription description = ProductDescription.of("Test Description");
            ImageUrl imageUrl = ImageUrl.of("https://example.com/image.jpg");
            Money money = new Money(eurCurrency, BigDecimal.valueOf(29.99));
            Quantity volume = Quantity.of(BigDecimal.valueOf(100));
            Instant now = Instant.now();
            Set<ProductCategory> categories = Set.of(
                ProductCategory.of(CategoryName.of("books")),
                ProductCategory.of(CategoryName.of("technology"))
            );

            ProductAggregate product = new ProductAggregate(sku, title, description, volume, money, now, now, imageUrl,
                categories);

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto).isNotNull();
            assertThat(dto.sku()).isEqualTo("TEST000001");
            assertThat(dto.title()).isEqualTo("Test Product");
            assertThat(dto.description()).isEqualTo("Test Description");
            assertThat(dto.imageUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(dto.price()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
            assertThat(dto.currency()).isEqualTo(eurCurrency);
            assertThat(dto.volume()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(dto.categories()).containsExactlyInAnyOrder("Books", "Technology");
            assertThat(dto.createdTime()).isEqualTo(now);
            assertThat(dto.updatedTime()).isEqualTo(now);
            assertThat(dto.inStock()).isTrue();
        }

        @Test
        @DisplayName("should map Product with empty categories")
        void toDTO_withEmptyCategories_shouldMapWithEmptySet() {
            // GIVEN
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000002"),
                ProductTitle.of("No Category Product"),
                ProductDescription.of("Product without categories"),
                Quantity.of(BigDecimal.valueOf(50)),
                new Money(eurCurrency, BigDecimal.valueOf(19.99)),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/nocategory.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.categories()).isEmpty();
        }

        @Test
        @DisplayName("should correctly map inStock flag when volume is zero")
        void toDTO_withZeroVolume_shouldMapInStockAsFalse() {
            // GIVEN
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000003"),
                ProductTitle.of("Out of Stock Product"),
                ProductDescription.of("Product with zero volume"),
                Quantity.of(BigDecimal.ZERO),
                new Money(eurCurrency, BigDecimal.valueOf(29.99)),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/outofstock.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.inStock()).isFalse();
        }

        @Test
        @DisplayName("should correctly map inStock flag when volume is positive")
        void toDTO_withPositiveVolume_shouldMapInStockAsTrue() {
            // GIVEN
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000004"),
                ProductTitle.of("In Stock Product"),
                ProductDescription.of("Product with positive volume"),
                Quantity.of(BigDecimal.valueOf(100)),
                new Money(eurCurrency, BigDecimal.valueOf(29.99)),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/instock.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.inStock()).isTrue();
        }

        @Test
        @DisplayName("should handle different currencies")
        void toDTO_withDifferentCurrency_shouldPreserveCurrency() {
            // GIVEN
            Currency usdCurrency = Currency.getInstance("USD");
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000005"),
                ProductTitle.of("USD Product"),
                ProductDescription.of("Product with USD currency"),
                Quantity.of(BigDecimal.valueOf(50)),
                new Money(usdCurrency, BigDecimal.valueOf(99.99)),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/usd.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.currency()).isEqualTo(usdCurrency);
            assertThat(dto.price()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        }

        @Test
        @DisplayName("should preserve decimal precision in price and volume")
        void toDTO_withDecimalValues_shouldPreservePrecision() {
            // GIVEN
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000006"),
                ProductTitle.of("Decimal Product"),
                ProductDescription.of("Product with decimal values"),
                Quantity.of(new BigDecimal("10.75")),
                new Money(eurCurrency, new BigDecimal("19.95")),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/decimal.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("19.95"));
            assertThat(dto.volume()).isEqualByComparingTo(new BigDecimal("10.75"));
        }
    }

    // ========== Round-Trip Mapping Tests ==========

    @Nested
    @DisplayName("Round-Trip Mapping Tests")
    class RoundTripMappingTests {

        @Test
        @DisplayName("should preserve data through toDomain and toDTO conversion")
        void roundTrip_shouldPreserveData() {
            // GIVEN
            CreateProductDto originalDto = new CreateProductDto(
                "TEST000001",
                "Round Trip Product",
                "Testing round trip conversion",
                "https://example.com/roundtrip.jpg",
                BigDecimal.valueOf(49.99),
                eurCurrency,
                BigDecimal.valueOf(75),
                Set.of("books", "technology")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(originalDto);
            ProductDTO resultDto = mapper.toDTO(product);

            // THEN
            assertThat(resultDto.sku()).isEqualTo(originalDto.sku());
            assertThat(resultDto.title()).isEqualTo(originalDto.title());
            assertThat(resultDto.description()).isEqualTo(originalDto.description());
            assertThat(resultDto.imageUrl()).isEqualTo(originalDto.imageUrl());
            assertThat(resultDto.price()).isEqualByComparingTo(originalDto.price());
            assertThat(resultDto.currency()).isEqualTo(originalDto.currency());
            assertThat(resultDto.volume()).isEqualByComparingTo(originalDto.volume());
            assertThat(resultDto.categories()).containsExactlyInAnyOrder("Books", "Technology");
        }
    }

    // ========== Collection Mapping Tests ==========

    @Nested
    @DisplayName("Collection Mapping Tests")
    class CollectionMappingTests {

        @Test
        @DisplayName("should map single category correctly")
        void mapCategories_withSingleCategory_shouldMapCorrectly() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000001",
                "Single Category",
                "Product with one category",
                "https://example.com/single.jpg",
                BigDecimal.valueOf(29.99),
                eurCurrency,
                BigDecimal.valueOf(100),
                Set.of("books")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);
            ProductDTO resultDto = mapper.toDTO(product);

            // THEN
            assertThat(resultDto.categories())
                .hasSize(1)
                .containsExactly("Books");
        }

        @Test
        @DisplayName("should map multiple categories correctly")
        void mapCategories_withMultipleCategories_shouldMapCorrectly() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000002",
                "Multiple Categories",
                "Product with multiple categories",
                "https://example.com/multiple.jpg",
                BigDecimal.valueOf(39.99),
                eurCurrency,
                BigDecimal.valueOf(50),
                Set.of("books", "technology", "programming", "education", "science")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);
            ProductDTO resultDto = mapper.toDTO(product);

            // THEN
            assertThat(resultDto.categories())
                .hasSize(5)
                .containsExactlyInAnyOrder("Books", "Technology", "Programming", "Education", "Science");
        }

        @Test
        @DisplayName("should handle empty category list")
        void mapCategories_withEmptyList_shouldMapToEmptySet() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000003",
                "No Categories",
                "Product without categories",
                "https://example.com/empty.jpg",
                BigDecimal.valueOf(19.99),
                eurCurrency,
                BigDecimal.valueOf(25),
                Set.of()
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);
            ProductDTO resultDto = mapper.toDTO(product);

            // THEN
            assertThat(resultDto.categories()).isEmpty();
        }
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle zero price")
        void toDTO_withZeroPrice_shouldMapCorrectly() {
            // GIVEN
            ProductAggregate product = new ProductAggregate(
                ProductSku.of("TEST000001"),
                ProductTitle.of("Free Product"),
                ProductDescription.of("Product with zero price"),
                Quantity.of(BigDecimal.valueOf(100)),
                new Money(eurCurrency, BigDecimal.ZERO),
                Instant.now(),
                Instant.now(),
                ImageUrl.of("https://example.com/free.jpg"),
                Set.of()
            );

            // WHEN
            ProductDTO dto = mapper.toDTO(product);

            // THEN
            assertThat(dto.price()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should handle large volume values")
        void toDomain_withLargeVolume_shouldMapCorrectly() {
            // GIVEN
            CreateProductDto dto = new CreateProductDto(
                "TEST000002",
                "Large Volume Product",
                "Product with large volume",
                "https://example.com/large.jpg",
                BigDecimal.valueOf(9.99),
                eurCurrency,
                new BigDecimal("1000000"),
                Set.of("books")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product.getVolume().getValue()).isEqualByComparingTo(new BigDecimal("1000000"));
        }

        @Test
        @DisplayName("should handle very long product descriptions")
        void toDomain_withLongDescription_shouldMapCorrectly() {
            // GIVEN
            String longDescription = "A".repeat(1000);
            CreateProductDto dto = new CreateProductDto(
                "TEST000003",
                "Long Description Product",
                longDescription,
                "https://example.com/long.jpg",
                BigDecimal.valueOf(29.99),
                eurCurrency,
                BigDecimal.valueOf(50),
                Set.of("books")
            );

            // WHEN
            ProductAggregate product = ProductApplicationMapper.toDomain(dto);

            // THEN
            assertThat(product.getDescription().getValue()).hasSize(1000);
        }
    }
}

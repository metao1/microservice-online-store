package com.metao.book.product.infrastructure.application;

import static com.metao.book.product.infrastructure.util.ProductConstant.CATEGORY;
import static com.metao.book.product.infrastructure.util.ProductConstant.SKU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.application.service.ProductApplicationService;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductDomainService;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductApplicationService Tests")
class ProductApplicationServiceTest {

    @InjectMocks
    ProductApplicationService productService;

    @Mock
    ProductDomainService productDomainService;

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Test
    void getProduct_whenProductNotFound_shouldThrowsException() {
        // WHEN
        when(productRepository.findBySku(ProductSku.of(SKU)))
            .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
            productService.getProductBySku(SKU)
        );

        verify(productRepository).findBySku(ProductSku.of(SKU));
    }

    @Test
    void testGetProductsByCategory() {
        var generatedProducts = ProductDtoGeneratorUtils.buildMultipleProducts(50)
            .stream()
            .map(ProductApplicationMapper::toDomain)
            .toList();

        when(productRepository.findByCategory(CategoryName.of(CATEGORY), 0, 50))
            .thenReturn(generatedProducts);

        var pes = productService.getProductsByCategory(CATEGORY, 0, 50);

        assertThat(pes)
            .isNotNull()
            .isNotEmpty()
            .hasSize(50)
            .usingRecursiveComparison()
            .isEqualTo(generatedProducts);
    }

    @Test
    void getProduct_whenProductFound_shouldReturnProduct() {
        // WHEN
        var pe = ProductDtoGeneratorUtils.buildOneProduct(
            SKU,
            "title",
            "description",
            CATEGORY
        );
        var product = ProductApplicationMapper.toDomain(pe);

        when(productRepository.findBySku(ProductSku.of(SKU)))
            .thenReturn(Optional.of(product));

        assertThat(productService.getProductBySku(SKU))
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(product);
    }

    @Test
    void saveProduct_whenProductIsUnique_shouldSaveProduct() {
        // GIVEN
        CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
        when(productDomainService.isProductUnique(SKU)).thenReturn(Boolean.TRUE);

        // WHEN
        productService.createProduct(createProductCommand(productDto));

        // THEN
        verify(productRepository).save(any());
    }

    @Test
    void saveProduct_whenProductIsNotUnique_shouldThrowException() {
        // GIVEN
        CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
        when(productDomainService.isProductUnique(SKU)).thenReturn(Boolean.FALSE);

        // WHEN
        assertThrows(IllegalArgumentException.class, () ->
            productService.createProduct(createProductCommand(productDto))
        );

        // THEN
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductExists_shouldUpdateProduct() {
        // GIVEN
        CreateProductDto originalProductDto = ProductDtoGeneratorUtils.buildOneProduct();
        var existingProduct = ProductApplicationMapper.toDomain(originalProductDto);
        when(productRepository.findBySku(ProductSku.of(SKU))).thenReturn(Optional.of(existingProduct));

        CreateProductDto updatedProductDto = ProductDtoGeneratorUtils.buildOneProduct(
            SKU,
            "updated title",
            "updated description",
            CATEGORY
        );

        // WHEN
        productService.updateProduct(updateProductCommand(updatedProductDto));

        // THEN
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getId())
            .isSameAs(existingProduct.getId());

        assertThat(savedProduct.getTitle())
            .isEqualTo(ProductTitle.of("updated title"));

        assertThat(savedProduct.getDescription())
            .isEqualTo(ProductDescription.of("updated description"));

        // This part of your test was already correct!
        assertThat(savedProduct.getCategories())
            .hasSize(1)
            .first()
            .satisfies(category -> assertThat(category.getName().value()).isEqualTo(CATEGORY));

    }

    // ========== Additional Create Product Scenarios ==========

    @Nested
    @DisplayName("Create Product Scenarios")
    class CreateProductScenarios {

        @Test
        @DisplayName("should create product with multiple categories")
        void createProduct_withMultipleCategories_shouldSaveProduct() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            CreateProductCommand command = new CreateProductCommand(
                productDto.sku(),
                productDto.title(),
                productDto.description(),
                productDto.imageUrl(),
                productDto.price(),
                productDto.currency(),
                productDto.volume(),
                Instant.now(),
                List.of("Books", "Technology", "Programming")
            );

            when(productDomainService.isProductUnique(SKU)).thenReturn(true);

            // WHEN
            productService.createProduct(command);

            // THEN
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();

            assertThat(savedProduct.getCategories()).hasSize(3);
        }

        @Test
        @DisplayName("should create product with no categories")
        void createProduct_withNoCategories_shouldSaveProduct() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            CreateProductCommand command = new CreateProductCommand(
                productDto.sku(),
                productDto.title(),
                productDto.description(),
                productDto.imageUrl(),
                productDto.price(),
                productDto.currency(),
                productDto.volume(),
                Instant.now(),
                List.of()
            );

            when(productDomainService.isProductUnique(SKU)).thenReturn(true);

            // WHEN
            productService.createProduct(command);

            // THEN
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();

            assertThat(savedProduct.getCategories()).isEmpty();
        }

        @Test
        @DisplayName("should create product with null category list")
        void createProduct_withNullCategoryList_shouldSaveProduct() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            CreateProductCommand command = new CreateProductCommand(
                productDto.sku(),
                productDto.title(),
                productDto.description(),
                productDto.imageUrl(),
                productDto.price(),
                productDto.currency(),
                productDto.volume(),
                Instant.now(),
                null
            );

            when(productDomainService.isProductUnique(SKU)).thenReturn(true);

            // WHEN
            productService.createProduct(command);

            // THEN
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();

            assertThat(savedProduct.getCategories()).isEmpty();
        }
    }

    // ========== Additional Update Product Scenarios ==========

    @Nested
    @DisplayName("Update Product Scenarios")
    class UpdateProductScenarios {

        @Test
        @DisplayName("should throw ProductNotFoundException when product not found")
        void updateProduct_whenProductNotFound_shouldThrowException() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            UpdateProductCommand command = updateProductCommand(productDto);

            when(productRepository.findBySku(ProductSku.of(SKU))).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(ProductNotFoundException.class, () ->
                productService.updateProduct(command)
            );

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update only price when other fields are same")
        void updateProduct_onlyPrice_shouldUpdateSuccessfully() {
            // GIVEN
            CreateProductDto originalDto = ProductDtoGeneratorUtils.buildOneProduct();
            Product existingProduct = ProductApplicationMapper.toDomain(originalDto);

            when(productRepository.findBySku(ProductSku.of(SKU))).thenReturn(Optional.of(existingProduct));

            UpdateProductCommand command = new UpdateProductCommand(
                SKU,
                originalDto.title(),
                originalDto.description(),
                BigDecimal.valueOf(99.99),
                Currency.getInstance("EUR")
            );

            // WHEN
            productService.updateProduct(command);

            // THEN
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();

            assertThat(savedProduct.getMoney())
                .isEqualTo(new Money(Currency.getInstance("EUR"), BigDecimal.valueOf(99.99)));
        }
    }

    // ========== Search and Query Scenarios ==========

    @Nested
    @DisplayName("Search and Query Scenarios")
    class SearchAndQueryScenarios {

        @Test
        @DisplayName("should search products with valid keyword")
        void searchProducts_withValidKeyword_shouldReturnProducts() {
            // GIVEN
            String keyword = "book";
            List<Product> expectedProducts = ProductDtoGeneratorUtils.buildMultipleProducts(5)
                .stream()
                .map(ProductApplicationMapper::toDomain)
                .toList();

            when(productRepository.searchByKeyword(keyword, 0, 10))
                .thenReturn(expectedProducts);

            // WHEN
            List<Product> results = productService.searchProducts(keyword, 0, 10);

            // THEN
            assertThat(results)
                .isNotNull()
                .hasSize(5)
                .usingRecursiveComparison()
                .isEqualTo(expectedProducts);
        }

        @Test
        @DisplayName("should return empty list when search keyword is null")
        void searchProducts_withNullKeyword_shouldReturnEmptyList() {
            // WHEN
            List<Product> results = productService.searchProducts(null, 0, 10);

            // THEN
            assertThat(results).isEmpty();
            verify(productRepository, never()).searchByKeyword(any(), any(Integer.class), any(Integer.class));
        }

        @Test
        @DisplayName("should get products by category with pagination")
        void getProductsByCategory_withPagination_shouldReturnProducts() {
            // GIVEN
            List<Product> expectedProducts = ProductDtoGeneratorUtils.buildMultipleProducts(20)
                .stream()
                .map(ProductApplicationMapper::toDomain)
                .toList();

            when(productRepository.findByCategory(CategoryName.of(CATEGORY), 10, 10))
                .thenReturn(expectedProducts);

            // WHEN
            List<Product> results = productService.getProductsByCategory(CATEGORY, 10, 10);

            // THEN
            assertThat(results).hasSize(20);
            verify(productRepository).findByCategory(CategoryName.of(CATEGORY), 10, 10);
        }

        @Test
        @DisplayName("should return empty list when category name is null")
        void getProductsByCategory_withNullCategoryName_shouldReturnEmptyList() {
            // WHEN
            List<Product> results = productService.getProductsByCategory(null, 0, 10);

            // THEN
            assertThat(results).isEmpty();
            verify(productRepository, never()).findByCategory(any(), any(Integer.class), any(Integer.class));
        }

        @Test
        @DisplayName("should get related products")
        void getRelatedProducts_withValidSku_shouldReturnRelatedProducts() {
            // GIVEN
            List<Product> relatedProducts = ProductDtoGeneratorUtils.buildMultipleProducts(5)
                .stream()
                .map(ProductApplicationMapper::toDomain)
                .toList();

            when(productDomainService.findRelatedProducts(ProductSku.of(SKU), 10))
                .thenReturn(relatedProducts);

            // WHEN
            List<Product> results = productService.getRelatedProducts(SKU, 10);

            // THEN
            assertThat(results).hasSize(5);
            verify(productDomainService).findRelatedProducts(ProductSku.of(SKU), 10);
        }

        @Test
        @DisplayName("should return empty list when SKU is null for related products")
        void getRelatedProducts_withNullSku_shouldReturnEmptyList() {
            // WHEN
            List<Product> results = productService.getRelatedProducts(null, 10);

            // THEN
            assertThat(results).isEmpty();
            verify(productDomainService, never()).findRelatedProducts(any(), any(Integer.class));
        }
    }

    // ========== Category Assignment Scenarios ==========

    @Nested
    @DisplayName("Category Assignment Scenarios")
    class CategoryAssignmentScenarios {

        @Test
        @DisplayName("should assign product to category successfully")
        void assignProductToCategory_withValidInputs_shouldAssignSuccessfully() {
            // GIVEN
            String categoryName = "NewCategory";

            // WHEN
            productService.assignProductToCategory(SKU, categoryName);

            // THEN
            verify(productDomainService).assignProductToCategory(
                ProductSku.of(SKU),
                CategoryName.of(categoryName)
            );
        }

        @Test
        @DisplayName("should do nothing when SKU is null")
        void assignProductToCategory_withNullSku_shouldDoNothing() {
            // WHEN
            productService.assignProductToCategory(null, "Category");

            // THEN
            verify(productDomainService, never()).assignProductToCategory(any(), any());
        }

        @Test
        @DisplayName("should do nothing when category name is null")
        void assignProductToCategory_withNullCategoryName_shouldDoNothing() {
            // WHEN
            productService.assignProductToCategory(SKU, null);

            // THEN
            verify(productDomainService, never()).assignProductToCategory(any(), any());
        }
    }

    // ========== Volume Management Scenarios ==========

    @Nested
    @DisplayName("Volume Management Scenarios")
    class VolumeManagementScenarios {

        @Test
        @DisplayName("should reduce product volume successfully")
        void reduceProductVolume_withValidInputs_shouldReduceSuccessfully() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            Product existingProduct = ProductApplicationMapper.toDomain(productDto);

            when(productRepository.findBySku(ProductSku.of(SKU)))
                .thenReturn(Optional.of(existingProduct));

            BigDecimal reduction = BigDecimal.valueOf(10);

            // WHEN
            productService.reduceProductVolume(SKU, reduction);

            // THEN
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when reducing volume of non-existent product")
        void reduceProductVolume_whenProductNotFound_shouldThrowException() {
            // GIVEN
            when(productRepository.findBySku(ProductSku.of(SKU)))
                .thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(ProductNotFoundException.class, () ->
                productService.reduceProductVolume(SKU, BigDecimal.valueOf(10))
            );

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should do nothing when SKU is null for volume reduction")
        void reduceProductVolume_withNullSku_shouldDoNothing() {
            // WHEN
            productService.reduceProductVolume(null, BigDecimal.valueOf(10));

            // THEN
            verify(productRepository, never()).findBySku(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should do nothing when quantity is null for volume reduction")
        void reduceProductVolume_withNullQuantity_shouldDoNothing() {
            // WHEN
            productService.reduceProductVolume(SKU, null);

            // THEN
            verify(productRepository, never()).findBySku(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should increase product volume successfully")
        void increaseProductVolume_withValidInputs_shouldIncreaseSuccessfully() {
            // GIVEN
            CreateProductDto productDto = ProductDtoGeneratorUtils.buildOneProduct();
            Product existingProduct = ProductApplicationMapper.toDomain(productDto);

            when(productRepository.findBySku(ProductSku.of(SKU)))
                .thenReturn(Optional.of(existingProduct));

            BigDecimal increase = BigDecimal.valueOf(50);

            // WHEN
            productService.increaseProductVolume(SKU, increase);

            // THEN
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when increasing volume of non-existent product")
        void increaseProductVolume_whenProductNotFound_shouldThrowException() {
            // GIVEN
            when(productRepository.findBySku(ProductSku.of(SKU)))
                .thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(ProductNotFoundException.class, () ->
                productService.increaseProductVolume(SKU, BigDecimal.valueOf(50))
            );

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should do nothing when SKU is null for volume increase")
        void increaseProductVolume_withNullSku_shouldDoNothing() {
            // WHEN
            productService.increaseProductVolume(null, BigDecimal.valueOf(50));

            // THEN
            verify(productRepository, never()).findBySku(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should do nothing when quantity is null for volume increase")
        void increaseProductVolume_withNullQuantity_shouldDoNothing() {
            // WHEN
            productService.increaseProductVolume(SKU, null);

            // THEN
            verify(productRepository, never()).findBySku(any());
            verify(productRepository, never()).save(any());
        }
    }

    // ========== Boundary and Error Cases ==========

    @Nested
    @DisplayName("Boundary and Error Cases")
    class BoundaryAndErrorCases {

        @Test
        @DisplayName("should throw IllegalArgumentException when getting product with null SKU")
        void getProductBySku_withNullSku_shouldThrowException() {
            // WHEN & THEN
            assertThrows(IllegalArgumentException.class, () ->
                productService.getProductBySku(null)
            );

            verify(productRepository, never()).findBySku(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when getting product with blank SKU")
        void getProductBySku_withBlankSku_shouldThrowException() {
            // WHEN & THEN
            assertThrows(IllegalArgumentException.class, () ->
                productService.getProductBySku("   ")
            );

            verify(productRepository, never()).findBySku(any());
        }

        @Test
        @DisplayName("should handle empty search results")
        void searchProducts_withNoMatches_shouldReturnEmptyList() {
            // GIVEN
            when(productRepository.searchByKeyword("nonexistent", 0, 10))
                .thenReturn(List.of());

            // WHEN
            List<Product> results = productService.searchProducts("nonexistent", 0, 10);

            // THEN
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should handle pagination at boundary")
        void getProductsByCategory_withHighOffset_shouldReturnEmptyList() {
            // GIVEN
            when(productRepository.findByCategory(CategoryName.of(CATEGORY), 1000, 10))
                .thenReturn(List.of());

            // WHEN
            List<Product> results = productService.getProductsByCategory(CATEGORY, 1000, 10);

            // THEN
            assertThat(results).isEmpty();
        }
    }

    // ========== Helper Methods ==========

    private UpdateProductCommand updateProductCommand(CreateProductDto dto) {
        return new UpdateProductCommand(
            dto.sku(),
            dto.title(),
            dto.description(),
            dto.price(),
            dto.currency()
        );
    }

    private CreateProductCommand createProductCommand(CreateProductDto dto) {
        return new CreateProductCommand(
            dto.sku(),
            dto.title(),
            dto.description(),
            dto.imageUrl(),
            dto.price(),
            dto.currency(),
            dto.volume(),
            Instant.now(),
            List.of(CATEGORY)
        );
    }
}
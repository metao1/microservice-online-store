package com.metao.book.product.domain.service;

import static com.metao.book.product.infrastructure.util.ProductConstant.SKU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.domain.exception.CategoryNotFoundException;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.shared.domain.base.DomainEventPublisher;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDomainService Tests")
class ProductAggregateDomainServiceTest {

    @InjectMocks
    private ProductDomainService productDomainService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    DomainEventPublisher eventPublisher;

    private ProductSku testSku;
    private CategoryName testCategoryName;
    private ProductCategory testCategory;
    private ProductAggregate testProduct;

    @BeforeEach
    void setUp() {
        testSku = ProductSku.of("TEST000001");
        testCategoryName = CategoryName.of("Electronics");
        testCategory = ProductCategory.of(CategoryId.of(UUID.randomUUID().toString()), testCategoryName);

        testProduct = createTestProduct(testSku, Set.of());
    }

    // ========== canAssignToCategory Tests ==========

    @Test
    @DisplayName("canAssignToCategory - when product has less than 5 categories - should return true")
    void canAssignToCategory_whenProductHasLessThan5Categories_shouldReturnTrue() {
        // GIVEN
        ProductAggregate product = createTestProduct(testSku, Set.of(
            createCategory("Category1"),
            createCategory("Category2"),
            createCategory("Category3")
        ));

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName(testCategoryName)).thenReturn(Optional.of(testCategory));

        // WHEN
        boolean canAssign = productDomainService.canAssignToCategory(testSku, testCategoryName);

        // THEN
        assertThat(canAssign).isTrue();
        verify(productRepository).findBySku(testSku);
        verify(categoryRepository).findByName(testCategoryName);
    }

    @Test
    @DisplayName("canAssignToCategory - when product has exactly 5 categories - should return false")
    void canAssignToCategory_whenProductHas5Categories_shouldReturnFalse() {
        // GIVEN
        ProductAggregate product = createTestProduct(testSku, Set.of(
            createCategory("Category1"),
            createCategory("Category2"),
            createCategory("Category3"),
            createCategory("Category4"),
            createCategory("Category5")
        ));

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName(testCategoryName)).thenReturn(Optional.of(testCategory));

        // WHEN
        boolean canAssign = productDomainService.canAssignToCategory(testSku, testCategoryName);

        // THEN
        assertThat(canAssign).isFalse();
    }

    @Test
    @DisplayName("canAssignToCategory - when product not found - should throw ProductNotFoundException")
    void canAssignToCategory_whenProductNotFound_shouldThrowProductNotFoundException() {
        // GIVEN
        when(productRepository.findBySku(testSku)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productDomainService.canAssignToCategory(testSku, testCategoryName))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining(testSku.value());

        verify(categoryRepository, never()).findByName(any());
    }

    @Test
    @DisplayName("canAssignToCategory - when category not found - should throw CategoryNotFoundException")
    void canAssignToCategory_whenCategoryNotFound_shouldThrowCategoryNotFoundException() {
        // GIVEN
        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findByName(testCategoryName)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productDomainService.canAssignToCategory(testSku, testCategoryName))
            .isInstanceOf(CategoryNotFoundException.class)
            .hasMessageContaining(testCategoryName.value());
    }

    // ========== assignProductToCategory Tests ==========

    @Test
    @DisplayName("assignProductToCategory - when valid assignment - should add category to product")
    void assignProductToCategory_whenValidAssignment_shouldAddCategoryToProduct() {
        // GIVEN
        ProductAggregate product = createTestProduct(testSku, new HashSet<>());

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName(testCategoryName)).thenReturn(Optional.of(testCategory));

        // WHEN
        productDomainService.assignProductToCategory(testSku, testCategoryName);

        // THEN
        assertThat(product.getCategories()).contains(testCategory);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("assignProductToCategory - when product has 5 categories - should throw IllegalStateException")
    void assignProductToCategory_whenProductHas5Categories_shouldThrowIllegalStateException() {
        // GIVEN
        ProductAggregate product = createTestProduct(testSku, Set.of(
            createCategory("Category1"),
            createCategory("Category2"),
            createCategory("Category3"),
            createCategory("Category4"),
            createCategory("Category5")
        ));

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName(testCategoryName)).thenReturn(Optional.of(testCategory));

        // WHEN & THEN
        assertThatThrownBy(() -> productDomainService.assignProductToCategory(testSku, testCategoryName))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cannot be assigned to more than 5 categories");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignProductToCategory - when product not found - should throw ProductNotFoundException")
    void assignProductToCategory_whenProductNotFound_shouldThrowProductNotFoundException() {
        // GIVEN
        when(productRepository.findBySku(testSku)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productDomainService.assignProductToCategory(testSku, testCategoryName))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    // ========== isProductUnique Tests ==========

    @Test
    @DisplayName("isProductUnique - when SKU does not exist - should return true")
    void isProductUnique_whenSkuDoesNotExist_shouldReturnTrue() {
        // GIVEN
        String sku = "UNIQUE0001";
        when(productRepository.findBySku(ProductSku.of(sku))).thenReturn(Optional.empty());

        // WHEN
        Boolean isUnique = productDomainService.isProductUnique(sku);

        // THEN
        assertThat(isUnique).isTrue();
    }

    @Test
    @DisplayName("isProductUnique - when SKU already exists - should return false")
    void isProductUnique_whenSkuExists_shouldReturnFalse() {
        // GIVEN
        String sku = "EXIST00001";
        ProductAggregate existingProduct = createTestProduct(SKU, Set.of());
        when(productRepository.findBySku(ProductSku.of(sku))).thenReturn(Optional.of(existingProduct));

        // WHEN
        Boolean isUnique = productDomainService.isProductUnique(sku);

        // THEN
        assertThat(isUnique).isFalse();
    }

    // ========== findRelatedProducts Tests ==========

    @Test
    @DisplayName("findRelatedProducts - when product has categories - should return products in same categories")
    void findRelatedProducts_whenProductHasCategories_shouldReturnRelatedProducts() {
        // GIVEN
        ProductCategory sharedCategory = createCategory("SharedCategory");
        ProductAggregate product = createTestProduct(testSku, Set.of(sharedCategory));

        ProductSku relatedSku1 = ProductSku.of("RELATED001");
        ProductSku relatedSku2 = ProductSku.of("RELATED002");
        ProductAggregate relatedProduct1 = createTestProduct(relatedSku1, Set.of(sharedCategory));
        ProductAggregate relatedProduct2 = createTestProduct(relatedSku2, Set.of(sharedCategory));

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(productRepository.findByCategories(any(), any(Integer.class), any(Integer.class)))
            .thenReturn(List.of(product, relatedProduct1, relatedProduct2));

        // WHEN
        List<ProductAggregate> relatedProducts = productDomainService.findRelatedProducts(testSku, 10);

        // THEN
        assertThat(relatedProducts)
            .hasSize(2)
            .doesNotContain(product)  // Original product should be filtered out
            .containsExactlyInAnyOrder(relatedProduct1, relatedProduct2);
    }

    @Test
    @DisplayName("findRelatedProducts - when product has no categories - should return empty list")
    void findRelatedProducts_whenProductHasNoCategories_shouldReturnEmptyList() {
        // GIVEN
        ProductAggregate product = createTestProduct(testSku, Set.of());
        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));

        // WHEN
        List<ProductAggregate> relatedProducts = productDomainService.findRelatedProducts(testSku, 10);

        // THEN
        assertThat(relatedProducts).isEmpty();
        verify(productRepository, never()).findByCategories(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("findRelatedProducts - when product not found - should throw ProductNotFoundException")
    void findRelatedProducts_whenProductNotFound_shouldThrowProductNotFoundException() {
        // GIVEN
        when(productRepository.findBySku(testSku)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productDomainService.findRelatedProducts(testSku, 10))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("findRelatedProducts - when limit specified - should respect limit")
    void findRelatedProducts_whenLimitSpecified_shouldRespectLimit() {
        // GIVEN
        ProductCategory sharedCategory = createCategory("SharedCategory");
        ProductAggregate product = createTestProduct(testSku, Set.of(sharedCategory));

        when(productRepository.findBySku(testSku)).thenReturn(Optional.of(product));
        when(productRepository.findByCategories(any(), any(Integer.class), any(Integer.class)))
            .thenReturn(List.of(product));

        int limit = 5;

        // WHEN
        productDomainService.findRelatedProducts(testSku, limit);

        // THEN
        verify(productRepository).findByCategories(any(), any(Integer.class), any(Integer.class));
    }

    // ========== Helper Methods ==========

    private ProductAggregate createTestProduct(ProductSku sku, Set<ProductCategory> categories) {
        return new ProductAggregate(
            sku,
            ProductTitle.of("Test Product"),
            ProductDescription.of("Test Description"),
            Quantity.of(BigDecimal.valueOf(100)),
            new Money(Currency.getInstance("EUR"), BigDecimal.valueOf(29.99)),
            Instant.now(),
            Instant.now(),
            ImageUrl.of("https://example.com/test.jpg"),
            categories
        );
    }

    private ProductCategory createCategory(String name) {
        return ProductCategory.of(
            CategoryId.of(UUID.randomUUID().toString()),
            CategoryName.of(name)
        );
    }
}

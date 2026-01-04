package com.metao.book.product.infrastructure.application;

import static com.metao.book.product.infrastructure.util.ProductConstant.CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductCategoriesService;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest {

    private static final String PRODUCT_ID = "1234567890";

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCategoriesService categoriesService;

    @Test
    @DisplayName("Get product categories found")
    void getProductCategoriesFound() {
        // GIVEN
        var returnedProductEntity = ProductDtoGeneratorUtils.buildOneProduct();
        var createdTime = Instant.now();
        var createdProduct = new Product(
            ProductSku.generate(),
            ProductTitle.of("title"),
            ProductDescription.of("description"),
            ProductVolume.of(BigDecimal.valueOf(12.00)),
            Money.of(
                BigDecimal.valueOf(12.00),
                Currency.getInstance("EUR")
            ),
            createdTime,
            createdTime,
            ImageUrl.of("https://example.com/image.jpg"),
            Set.of(ProductCategory.of(CategoryId.of(UUID.randomUUID().toString()), CategoryName.of(CATEGORY)))
        );
        when(productRepository.findBySku(ProductSku.of(PRODUCT_ID)))
            .thenReturn(Optional.of(createdProduct));

        // WHEN
        Set<ProductCategory> productCategories = categoriesService.getProductCategories(PRODUCT_ID);

        // THEN
        assertThat(productCategories)
            .isNotNull()
            .describedAs("Product categories should be returned")
            .isNotEmpty()
            .hasSize(1)
            .extracting(item -> item.getName().value())
            .isEqualTo(List.of(CATEGORY));
    }

    @Test
    @SneakyThrows
    @DisplayName("Get product categories not found")
    void testGetProductCategoriesNotFound() {
        assertThatThrownBy(() -> categoriesService.getProductCategories(PRODUCT_ID))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining(PRODUCT_ID);
    }
}
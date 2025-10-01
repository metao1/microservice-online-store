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
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;
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
        when(productRepository.findBySku(ProductSku.of(PRODUCT_ID)))
            .thenReturn(Optional.of(
                Product.reconstruct(
                    ProductSku.generate(),
                    ProductTitle.of("title"),
                    ProductDescription.of("description"),
                    ProductVolume.of(new BigDecimal("12.00")),
                    Money.of(
                        new BigDecimal("12.00"),
                        Currency.getInstance("EUR")
                    ),
                    ImageUrl.of("https://example.com/image.jpg"),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    Set.of(ProductCategory.of(CategoryId.of(1L), CategoryName.of(CATEGORY)))
                )
            ));

        // WHEN
        Set<ProductCategory> productCategories = categoriesService.getProductCategories(PRODUCT_ID);

        // THEN
        assertThat(productCategories)
            .isNotNull()
            .isEqualTo(Set.of(ProductCategory.of(CategoryId.of(1L), CategoryName.of("book"))));
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
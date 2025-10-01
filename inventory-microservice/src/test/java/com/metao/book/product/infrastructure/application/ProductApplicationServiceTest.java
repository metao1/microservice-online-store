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
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.application.service.ProductApplicationService;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductDomainService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
        verify(productRepository, never()).save(any(com.metao.book.product.domain.model.aggregate.Product.class));
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
            List.of(CATEGORY)
        );
    }
}
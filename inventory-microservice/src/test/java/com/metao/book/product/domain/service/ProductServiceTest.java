package com.metao.book.product.domain.service;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.shared.domain.financial.Money; // Correct import for Money
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Currency;
import java.util.Optional; // Added import

import com.metao.book.product.domain.exception.ProductNotFoundException; // Added import
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows; // Added import
// import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    // ProductMapper is a class with static methods, so no need to mock if used as ProductMapper.toDto

    @InjectMocks
    private ProductService productService;

    private Product product1;
    // private ProductDTO productDTO1; // Not explicitly needed as it's created during the service call

    @BeforeEach
    void setUp() {
        // Product(asin, title, description, volume, money, imageUrl)
        product1 = new Product("ASIN001", "Test Title 1", "Description for test", 
                               BigDecimal.ONE, 
                               new com.metao.book.shared.domain.financial.Money(Currency.getInstance("USD"), BigDecimal.TEN), 
                               "http://example.com/image1.jpg");
        // productDTO1 will be implicitly created by the mapper in the service method.
    }

    @Test
    void searchProductsByKeyword_whenResultsFound_returnsDtoList() {
        String keyword = "Test";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit); // Spring Data JPA pages are 0-indexed for offset
        when(productRepository.searchByKeyword(eq(keyword), eq(pageable))).thenReturn(List.of(product1));

        List<ProductDTO> results = productService.searchProductsByKeyword(keyword, offset, limit);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        
        // Assertions based on ProductMapper.toDto logic
        ProductDTO resultDTO = results.get(0);
        assertThat(resultDTO.asin()).isEqualTo(product1.getAsin());
        assertThat(resultDTO.title()).isEqualTo(product1.getTitle());
        assertThat(resultDTO.description()).isEqualTo(product1.getDescription());
        assertThat(resultDTO.volume()).isEqualByComparingTo(product1.getVolume());
        assertThat(resultDTO.price()).isEqualByComparingTo(product1.getPriceValue());
        assertThat(resultDTO.currency()).isEqualTo(product1.getPriceCurrency());
        assertThat(resultDTO.imageUrl()).isEqualTo(product1.getImageUrl());
    }

    @Test
    void searchProductsByKeyword_whenNoResultsFound_returnsEmptyList() {
        String keyword = "NonExistent";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        when(productRepository.searchByKeyword(eq(keyword), eq(pageable))).thenReturn(Collections.emptyList());

        List<ProductDTO> results = productService.searchProductsByKeyword(keyword, offset, limit);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    // --- Tests for getProductByAsin ---
    @Test
    void getProductByAsin_whenProductExists_returnsOptionalOfProduct() {
        String asinToTest = product1.getAsin();
        when(productRepository.findByAsin(asinToTest)).thenReturn(Optional.of(product1));

        Optional<Product> result = productService.getProductByAsin(asinToTest);

        assertThat(result).isPresent();
        assertThat(result.get().getAsin()).isEqualTo(asinToTest);
    }

    @Test
    void getProductByAsin_whenProductDoesNotExist_throwsProductNotFoundException() {
        String nonExistentAsin = "NONEXISTENTASIN";
        when(productRepository.findByAsin(nonExistentAsin)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductByAsin(nonExistentAsin);
        });
    }

    // --- Tests for getProductsByCategory ---
    @Test
    void getProductsByCategory_whenProductsExist_returnsDtoList() {
        String categoryName = "TestCategory";
        int offset = 0;
        int limit = 10;
        // The Pageable in ProductService.getProductsByCategory uses OffsetBasedPageRequest,
        // but for mocking repository, PageRequest.of is fine as it's about the interface.
        Pageable pageable = PageRequest.of(offset, limit); 
        when(productRepository.findAllByCategories(eq(categoryName), eq(pageable))).thenReturn(List.of(product1));

        List<ProductDTO> results = productService.getProductsByCategory(limit, offset, categoryName);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).asin()).isEqualTo(product1.getAsin());
        assertThat(results.get(0).title()).isEqualTo(product1.getTitle());
    }

    @Test
    void getProductsByCategory_whenNoProductsExist_returnsEmptyList() {
        String categoryName = "EmptyCategory";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        when(productRepository.findAllByCategories(eq(categoryName), eq(pageable))).thenReturn(Collections.emptyList());

        List<ProductDTO> results = productService.getProductsByCategory(limit, offset, categoryName);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }
}
```

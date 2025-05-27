package com.metao.book.product.domain.service;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.shared.domain.financial.Money;
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
import java.util.Optional;

import com.metao.book.product.domain.exception.ProductNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = new Product("ASIN001", "Test Title 1", "Description for test", 
                               BigDecimal.ONE, 
                               new com.metao.book.shared.domain.financial.Money(Currency.getInstance("USD"), BigDecimal.TEN), 
                               "http://example.com/image1.jpg");
    }

    @Test
    void searchProductsByKeyword_whenResultsFound_returnsDtoList() {
        String keyword = "Test";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        when(productRepository.searchByKeyword(eq(keyword), eq(pageable))).thenReturn(List.of(product1));

        List<ProductDTO> results = productService.searchProductsByKeyword(keyword, offset, limit);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        
        ProductDTO resultDTO = results.get(0);
        assertThat(resultDTO.asin()).isEqualTo(product1.getAsin());
        assertThat(resultDTO.title()).isEqualTo(product1.getTitle());
        assertThat(resultDTO.description()).isEqualTo(product1.getDescription());
        // Assuming ProductMapper correctly maps volume, price, currency, imageUrl based on Product entity getters
        // For example:
        assertThat(resultDTO.volume()).isEqualByComparingTo(product1.getVolume());
        assertThat(resultDTO.price()).isEqualByComparingTo(product1.getPriceValue()); // Assuming getPriceValue() for BigDecimal price
        assertThat(resultDTO.currency()).isEqualTo(product1.getPriceCurrency()); // Assuming getPriceCurrency()
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

        // As per worker's report on previous test creation, this service method throws ProductNotFoundException
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductByAsin(nonExistentAsin);
        });
    }

    // --- Tests for getProductsByCategories ---
    @Test
    void getProductsByCategories_singleCategory_whenProductsExist_returnsDtoList() {
        String categoryName = "TestCategory";
        List<String> categoryNames = List.of(categoryName); // Pass as list
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit); 
        // Mock repository to expect a list
        when(productRepository.findAllByCategories(eq(categoryNames), eq(pageable))).thenReturn(List.of(product1));

        List<ProductDTO> results = productService.getProductsByCategories(limit, offset, categoryNames); // Call new method

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).asin()).isEqualTo(product1.getAsin());
        assertThat(results.get(0).title()).isEqualTo(product1.getTitle());
    }

    @Test
    void getProductsByCategories_multipleCategories_whenProductsExist_returnsDtoList() {
        List<String> categoryNames = List.of("Category1", "Category2");
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        // Assume product1 would be returned if it matched any of these categories
        when(productRepository.findAllByCategories(eq(categoryNames), eq(pageable))).thenReturn(List.of(product1)); 

        List<ProductDTO> results = productService.getProductsByCategories(limit, offset, categoryNames);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1); // Or more if more products are mocked
        assertThat(results.get(0).asin()).isEqualTo(product1.getAsin());
    }

    @Test
    void getProductsByCategories_singleCategory_whenNoProductsExist_returnsEmptyList() {
        String categoryName = "EmptyCategory";
        List<String> categoryNames = List.of(categoryName); // Pass as list
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        // Mock repository to expect a list
        when(productRepository.findAllByCategories(eq(categoryNames), eq(pageable))).thenReturn(Collections.emptyList());

        List<ProductDTO> results = productService.getProductsByCategories(limit, offset, categoryNames); // Call new method

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void getProductsByCategories_whenCategoryListIsEmpty_returnsEmptyList() {
        List<String> categoryNames = Collections.emptyList();
        int offset = 0;
        int limit = 10;
        // No need to mock repository as service should return early

        List<ProductDTO> results = productService.getProductsByCategories(limit, offset, categoryNames);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void getProductsByCategories_whenCategoryListIsNull_returnsEmptyList() {
        int offset = 0;
        int limit = 10;
        // No need to mock repository as service should return early

        List<ProductDTO> results = productService.getProductsByCategories(limit, offset, null);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }
}
```

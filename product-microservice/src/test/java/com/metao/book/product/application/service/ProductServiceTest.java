package com.metao.book.product.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.exception.ProductNotFoundException;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.util.ProductTestUtils;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepo;

    @InjectMocks
    ProductService productService;

    @Test
    void getProductById_notFound() {
        assertThrows(ProductNotFoundException.class,
            () -> productService.getProductById(new ProductId("1")));
    }

    @Test
    void getProductById_isFound() {
        var pe = ProductTestUtils.createProductEntity();
        var id = "1";
        when(productRepo.findById(new ProductId(id)))
            .thenReturn(Optional.of(pe));
        var product = productService.getProductById(new ProductId(id));
        Assertions.assertNotNull(product);
    }

    @Test
    void testSaveProduct() {
        var pe = ProductTestUtils.createProductEntity();
        productService.saveProduct(pe);
        verify(productRepo).save(pe);
    }
}

package com.metao.book.retails.application.service;

import com.metao.book.retails.application.exception.ProductNotFoundException;
import com.metao.book.retails.domain.ProductRepository;
import com.metao.book.retails.util.ProductTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

        @Mock
        ProductRepository productRepo;

        @InjectMocks
        ProductService productService;

        @Test
        void getProductById_notFound() {
                assertThrows(ProductNotFoundException.class,
                                () -> productService.getProductById(1L));
        }

        @Test
        void getProductById_isFound() {
                var pe = ProductTestUtils.createProductEntity();
                var productId = 1L;
                when(productRepo.findById(productId))
                                .thenReturn(Optional.of(pe));
                var product = productService.getProductById(productId);
                Assertions.assertNotNull(product);
        }

        @Test
        void testSaveProduct() {
                var pe = ProductTestUtils.createProductEntity();
                productService.saveProduct(pe);
                verify(productRepo).save(pe);
        }
}

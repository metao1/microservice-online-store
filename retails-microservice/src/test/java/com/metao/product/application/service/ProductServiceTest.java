package com.metao.product.application.service;

import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.exception.ProductNotFoundException;
import com.metao.product.application.persistence.ProductRepository;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.category.CategoryEntity;
import com.metao.product.domain.image.Image;
import com.metao.product.util.ProductTestUtils;

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
        void testGetAllProductsPageable() {

        }

        @Test
        void getProductById_notFound() {
                Assertions.assertThrows(ProductNotFoundException.class, ()->
                        productService.getProductById(new ProductId("asin"))
                );
        }

        @Test
        void getProductById_isFound() {
                var pe = ProductTestUtils.createProductEntity();  
                var productId = new ProductId("asin"); 
                when(productRepo.findProductEntityById(productId))
                        .thenReturn(Optional.of(pe));
                var product = productService.getProductById(new ProductId("asin"));
                Assertions.assertNotNull(product);
        }

        @Test
        void testSaveProduct() {

                productService.saveProduct(pe);
        }
}

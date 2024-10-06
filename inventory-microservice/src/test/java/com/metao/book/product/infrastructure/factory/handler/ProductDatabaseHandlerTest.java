package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.Mockito.verify;

import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDatabaseHandlerTest {

    @InjectMocks
    ProductMapper productMapper;

    @Mock
    ProductService productService;

    @Test
    void testOnProductCreatedEventThenSavesInDatabase() {
        var productMsgHandler = new ProductDatabaseHandler(productService, productMapper);
        var event = ProductTestUtils.productCreatedEvent();
        var entity = productMapper.toEntity(event);

        productMsgHandler.accept(event);

        verify(productService).saveProduct(entity);

    }
}

package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.Mockito.verify;

import com.metao.book.product.application.service.EventValidator;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.util.ProductEntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDatabaseHandlerTest {

    @Mock
    ProductService productService;

    @Mock
    EventValidator validator;

    @Test
    void testOnProductCreatedEventThenSavesInDatabase() {
        var productHandler = new ProductDatabaseHandler(productService, validator);
        var event = ProductEntityUtils.productCreatedEvent();
        var entity = ProductMapper.fromProductCreatedEvent(event);
        productHandler.accept(event);
        verify(productService).saveProduct(entity);
    }
}

package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.infrastructure.mapper.ProductMapper;
import com.metao.book.product.infrastructure.util.EventUtil;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductMessageHandlerTest {

    @Mock
    ProductRepository productRepo;

    @Test
    void testOnMessage() {
        var productMapper = new ProductMapper();
        var productService = new ProductService(productRepo);
        var productMsgHandler = new ProductMessageHandler(productService, productMapper);
        var product = ProductTestUtils.createProductDTO();
        var entity = productMapper.toEntity(product);
        var event = EventUtil.createEvent(product);
        productMsgHandler.onMessage(event);
        verify(productRepo).save(argThat(new EventMatcher(entity.orElseThrow())));
    }

    private static class EventMatcher implements ArgumentMatcher<ProductEntity> {

        private final ProductEntity entity;

        public EventMatcher(ProductEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean matches(ProductEntity argument) {
            return entity.getTitle().equals(argument.getTitle()) &&
                    entity.getDescription().equals(argument.getDescription()) &&
                    entity.getPriceCurrency().equals(argument.getPriceCurrency()) &&
                    entity.getImage().equals(argument.getImage());
        }

    }
}

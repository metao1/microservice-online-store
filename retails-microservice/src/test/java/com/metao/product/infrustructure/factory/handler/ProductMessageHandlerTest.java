package com.metao.product.infrustructure.factory.handler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import com.metao.product.application.persistence.ProductRepository;
import com.metao.product.application.service.ProductService;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.mapper.ProductMapper;
import com.metao.product.util.ProductTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                var entity =productMapper.toEntity(product);
                var event = new CreateProductEvent(product, Instant.now(), Instant.now());
                productMsgHandler.onMessage(event);
                verify(productRepo).save(argThat(new EventMatcher(entity.orElseThrow())));
        }

        private class  EventMatcher implements ArgumentMatcher<ProductEntity> {

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

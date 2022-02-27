package com.metao.product.infrustructure.factory.handler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.metao.ddd.finance.Currency;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.application.persistence.ProductRepositoryImpl;
import com.metao.product.application.service.ProductService;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.infrustructure.mapper.ProductMapperImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductMessageHandlerTest {

        @Mock
        ProductRepositoryImpl productRepo;
        
        @Test
        void testOnMessage() {
                var productMapper = new ProductMapperImpl();        
                var productService = new ProductService(productRepo);
                var productMsgHandler = new ProductMessageHandler(productService, productMapper);
                var product = ProductDTO
                                .builder()
                                .currency(Currency.DLR)
                                .asin("asin")
                                .title("title")
                                .description("description")
                                .price(1000d)
                                .categories(Set.of(CategoryDTO.of("book")))
                                .build();
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

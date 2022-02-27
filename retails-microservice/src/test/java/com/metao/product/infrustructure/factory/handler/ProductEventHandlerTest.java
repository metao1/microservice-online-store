package com.metao.product.infrustructure.factory.handler;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Set;

import com.metao.ddd.finance.Currency;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.event.CreateProductEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductEventHandlerTest {
        
        @InjectMocks
        ProductEventHandler eventHandler;
        
        @Mock
        LogMessageHandler messageHandler;

        @Test
        void whenCreateProductEvent_sendEvent_isOk() {
                eventHandler.addMessageHandler(messageHandler);
                var productDto = ProductDTO
                                .builder()
                                .currency(Currency.DLR)
                                .asin("asin")
                                .title("title")
                                .description("description")
                                .price(1000d)
                                .categories(Set.of(CategoryDTO.of("book")))
                                .build();
                
                var event = new CreateProductEvent(productDto, Instant.now(), Instant.now());
                
                eventHandler.sendEvent(event);            
                verify(messageHandler).onMessage(event);
        }
}

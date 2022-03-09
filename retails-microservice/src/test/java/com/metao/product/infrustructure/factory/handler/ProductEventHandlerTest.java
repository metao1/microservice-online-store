package com.metao.product.infrustructure.factory.handler;

import static org.mockito.Mockito.verify;

import java.time.Instant;

import com.metao.product.domain.event.CreateProductEvent;
import com.metao.product.util.ProductTestUtils;

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
                var productDto = ProductTestUtils.createProductDTO();
                
                var event = new CreateProductEvent(productDto, Instant.now(), Instant.now());
                
                eventHandler.sendEvent(event);            
                verify(messageHandler).onMessage(event);
        }
}

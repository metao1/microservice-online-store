package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.retails.domain.event.CreateProductEvent;
import com.metao.book.retails.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

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

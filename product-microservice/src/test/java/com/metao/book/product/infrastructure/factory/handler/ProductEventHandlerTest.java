package com.metao.book.product.infrastructure.factory.handler;

import com.metao.book.product.infrastructure.util.EventUtil;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        var event = EventUtil.createEvent(productDto);

        eventHandler.sendEvent(event);
        verify(messageHandler).onMessage(event);
    }
}

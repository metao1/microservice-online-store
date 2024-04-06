package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.Mockito.verify;

import com.metao.book.product.infrastructure.util.EventUtil;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductEventHandlerTest {

    @InjectMocks
    ProductEventHandler eventHandler;

    @Mock
    LogMessageHandler messageHandler;

    @Test
    void whenCreateProductEvent_sendEvent_isOk() {
        eventHandler.addMessageHandler(messageHandler);
        var productDto = ProductTestUtils.createProductDTO();

        var event = EventUtil.createProductEvent(productDto);

        eventHandler.publish(event);
        verify(messageHandler).onMessage(event);
    }
}

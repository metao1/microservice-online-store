package com.metao.book.product.infrastructure.factory.handler;

import static org.mockito.Mockito.verify;

import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventHandlerTest {

    @Mock
    EventHandler<ProductCreatedEvent> eventHandler;

    @Test
    void whenCreateProductEventSendEventOk() {
        eventHandler.subscribe((event)-> System.out.println(event));
        var event = ProductTestUtils.productCreatedEvent();

        eventHandler.publish(event);
        verify(eventHandler).publish(event);
    }
}

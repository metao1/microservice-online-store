package com.metao.book.payment.listener;

import static org.mockito.Mockito.verify;

import com.metao.book.payment.application.usecase.HandleOrderCreatedEventCommand;
import com.metao.book.payment.application.usecase.HandleOrderCreatedEventUseCase;
import com.metao.book.shared.OrderCreatedEvent;
import java.math.BigDecimal;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private HandleOrderCreatedEventUseCase handleOrderCreatedEventUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private OrderCreatedEventListener eventListener;

    @Test
    void handleOrderCreatedEventDelegatesToUseCase() {
        OrderCreatedEvent orderEvent = OrderCreatedEvent.newBuilder()
            .setId("order-123")
            .addItems(OrderCreatedEvent.OrderItem.newBuilder()
                .setSku("SKU-1")
                .setProductTitle("Book 1")
                .setQuantity(2d)
                .setPrice(10d)
                .setCurrency("EUR")
                .build())
            .addItems(OrderCreatedEvent.OrderItem.newBuilder()
                .setSku("SKU-2")
                .setProductTitle("Book 2")
                .setQuantity(1d)
                .setPrice(5d)
                .setCurrency("EUR")
                .build())
            .build();

        eventListener.handleOrderCreatedEvent(orderEvent, acknowledgment);

        ArgumentCaptor<HandleOrderCreatedEventCommand> commandCaptor = ArgumentCaptor.forClass(
            HandleOrderCreatedEventCommand.class);
        verify(handleOrderCreatedEventUseCase).handle(commandCaptor.capture());
        HandleOrderCreatedEventCommand command = commandCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertAll(
            () -> org.junit.jupiter.api.Assertions.assertEquals("order-123", command.eventId()),
            () -> org.junit.jupiter.api.Assertions.assertEquals("order-123", command.orderId()),
            () -> org.junit.jupiter.api.Assertions.assertEquals("EUR", command.currency()),
            () -> org.junit.jupiter.api.Assertions.assertEquals(0, command.amount().compareTo(BigDecimal.valueOf(25.0d)))
        );
        verify(acknowledgment).acknowledge();
    }
}

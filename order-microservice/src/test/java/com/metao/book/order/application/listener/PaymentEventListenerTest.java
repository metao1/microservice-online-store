package com.metao.book.order.infrastructure.messaging.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.metao.book.order.application.usecase.HandleOrderPaymentEventCommand;
import com.metao.book.order.application.usecase.HandleOrderPaymentEventUseCase;
import com.metao.book.order.domain.exception.InvalidPaymentEventException;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventListener")
class PaymentEventListenerTest {

    @Mock
    private HandleOrderPaymentEventUseCase handleOrderPaymentEventUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Nested
    @DisplayName("Event mapping")
    class EventMapping {

        @Test
        @DisplayName("Should delegate to use case using immutable event id")
        void shouldDelegateUsingPaymentIdAsEventId() {
            OrderPaymentUpdatedEvent paymentEvent = OrderPaymentUpdatedEvent.newBuilder()
                .setOrderId("order123")
                .setPaymentId("payment-1")
                .setEventId("event-1")
                .setStatus(Status.SUCCESSFUL)
                .build();

            paymentEventListener.handlePaymentEvent(paymentEvent, acknowledgment);

            verify(handleOrderPaymentEventUseCase).handle(
                new HandleOrderPaymentEventCommand("event-1", "order123", "SUCCESSFUL")
            );
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("Should reject events without an immutable event id")
        void shouldRejectEventWithoutImmutableEventId() {
            OrderPaymentUpdatedEvent paymentEvent = OrderPaymentUpdatedEvent.newBuilder()
                .setOrderId("order123")
                .setPaymentId("")
                .setStatus(Status.FAILED)
                .build();

            assertThrows(InvalidPaymentEventException.class,
                () -> paymentEventListener.handlePaymentEvent(paymentEvent, acknowledgment));
        }
    }
}

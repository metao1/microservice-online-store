package com.metao.book.payment.application.service;

import com.metao.book.payment.application.port.ConsumedMessagePort;
import com.metao.book.payment.application.usecase.HandleOrderCreatedEventCommand;
import com.metao.book.payment.application.usecase.HandleOrderCreatedEventUseCase;
import com.metao.book.payment.application.usecase.PaymentUseCase;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleOrderCreatedEventService implements HandleOrderCreatedEventUseCase {

    private final PaymentUseCase paymentUseCase;
    private final ConsumedMessagePort consumedMessagePort;

    @Override
    @Transactional
    @Timed(value = "payment.application.handle-order-created-event")
    public void handle(HandleOrderCreatedEventCommand command) {
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new IllegalArgumentException("Order-created event ID must not be blank");
        }
        if (!consumedMessagePort.claim("payment.order-created", command.eventId())) {
            return;
        }
        paymentUseCase.processOrderCreatedEvent(command.orderId(), command.amount(), command.currency());
    }
}

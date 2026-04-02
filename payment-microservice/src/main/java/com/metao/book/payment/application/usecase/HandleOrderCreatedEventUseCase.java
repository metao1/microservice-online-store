package com.metao.book.payment.application.usecase;

import com.metao.book.payment.application.port.ProcessedOrderCreatedEventPort;
import com.metao.book.payment.application.service.PaymentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class HandleOrderCreatedEventUseCase {

    private final PaymentApplicationService paymentApplicationService;
    private final ProcessedOrderCreatedEventPort processedOrderCreatedEventPort;

    @Transactional
    public void handle(HandleOrderCreatedEventCommand command) {
        log.info("Received aggregated OrderCreatedEvent for order: {}", command.orderId());

        if (!processedOrderCreatedEventPort.markProcessed(command.eventId())) {
            log.info("OrderCreatedEvent {} already processed; skipping duplicate.", command.eventId());
            return;
        }

        var paymentProcessed = paymentApplicationService.processOrderCreatedEvent(
            command.orderId(),
            command.amount(),
            command.currency()
        );

        if (paymentProcessed.isSuccessful()) {
            log.info("Sent OrderPaymentEvent for order: {}", command.orderId());
        }
    }
}

package com.metao.book.order.infrastructure.listener;

import com.metao.book.order.application.usecase.HandleOrderPaymentEventCommand;
import com.metao.book.order.application.usecase.HandleOrderPaymentEventUseCase;

import com.metao.book.shared.OrderPaymentUpdatedEvent;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final HandleOrderPaymentEventUseCase handleOrderPaymentEventUseCase;

    @KafkaListener(
        id = "${kafka.topic.order-payment.id}",
        topics = "${kafka.topic.order-payment.name}",
        groupId = "${kafka.topic.order-payment.group-id}",
        containerFactory = "orderPaymentEventKafkaListenerContainerFactory"
    )
    @Timed(value = "order.payment.listener", extraTags = {"listener", "order-payment"})
    public void handlePaymentEvent(OrderPaymentUpdatedEvent paymentEvent, Acknowledgment acknowledgment) {
        handleOrderPaymentEventUseCase.handle(new HandleOrderPaymentEventCommand(
            resolveEventId(paymentEvent),
            paymentEvent.getOrderId(),
            paymentEvent.getStatus().name()
        ));
        acknowledgment.acknowledge();
    }

    private String resolveEventId(OrderPaymentUpdatedEvent paymentEvent) {
        if (!paymentEvent.getPaymentId().isBlank()) {
            return paymentEvent.getPaymentId();
        }
        long seconds = paymentEvent.hasUpdatedTime() ? paymentEvent.getUpdatedTime().getSeconds() : 0L;
        int nanos = paymentEvent.hasUpdatedTime() ? paymentEvent.getUpdatedTime().getNanos() : 0;
        return paymentEvent.getOrderId() + ":" + paymentEvent.getStatus().name() + ":" + seconds + ":" + nanos;
    }
}

package com.metao.book.order.infrastructure.messaging.kafka.consumer;

import com.metao.book.order.application.usecase.HandleOrderPaymentEventCommand;
import com.metao.book.order.application.usecase.HandleOrderPaymentEventUseCase;
import com.metao.book.order.domain.exception.InvalidPaymentEventException;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.architecture.InboundAdapter;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@InboundAdapter(InboundAdapter.Kind.MESSAGING)
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
        if (paymentEvent.getEventId().isBlank()) {
            throw new InvalidPaymentEventException("Payment event ID must not be blank");
        }
        handleOrderPaymentEventUseCase.handle(new HandleOrderPaymentEventCommand(
            paymentEvent.getEventId(),
            paymentEvent.getOrderId(),
            paymentEvent.getStatus().name()
        ));
        acknowledgment.acknowledge();
    }
}

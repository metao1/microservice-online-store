package com.metao.book.payment.infrastructure.messaging;

import com.google.protobuf.Timestamp;
import com.metao.book.payment.domain.model.event.PaymentFailedEvent;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedEventEventTranslator implements ProtobufDomainEventTranslator {

    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        PaymentFailedEvent domainEvent = (PaymentFailedEvent) event;

        OrderPaymentUpdatedEvent message = OrderPaymentUpdatedEvent.newBuilder()
                .setId(domainEvent.getEventId())
                .setEventId(domainEvent.getEventId())
                .setPaymentId(domainEvent.getPaymentId().value())
                .setOrderId(domainEvent.getOrderId().value())
                .setStatus(Status.FAILED)
                .setErrorMessage(domainEvent.getFailureReason())
                .setUpdatedTime(Timestamp.newBuilder()
                        .setSeconds(domainEvent.getOccurredOn().atZone(ZoneOffset.UTC).toEpochSecond())
                        .setNanos(domainEvent.getOccurredOn().getNano())
                        .build())
                .build();

        return new ProtobufTranslation(
            "payment",
            domainEvent.getPaymentId().value(),
            "payment.failed",
            1,
            domainEvent.getOrderId().value(),
            message.toByteArray(),
            "order-payment:" + domainEvent.getOrderId().value()
        );
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof PaymentFailedEvent;
    }
}

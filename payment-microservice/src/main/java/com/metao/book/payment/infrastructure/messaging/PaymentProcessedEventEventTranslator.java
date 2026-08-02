package com.metao.book.payment.infrastructure.messaging;

import com.google.protobuf.Timestamp;
import com.metao.book.payment.domain.model.event.PaymentProcessedEvent;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufDomainEventTranslator;
import com.metao.book.shared.infrastructure.messaging.protobuf.ProtobufTranslation;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessedEventEventTranslator implements ProtobufDomainEventTranslator {

    @Override
    public ProtobufTranslation translate(DomainEvent event) {
        PaymentProcessedEvent domainEvent = (PaymentProcessedEvent) event;

        OrderPaymentUpdatedEvent message = OrderPaymentUpdatedEvent.newBuilder()
            .setId(domainEvent.getEventId())
            .setEventId(domainEvent.getEventId())
            .setPaymentId(domainEvent.getPaymentId().value())
            .setOrderId(domainEvent.getOrderId().value())
            .setStatus(mapStatus(domainEvent.getStatus()))
            .setUpdatedTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();

        return new ProtobufTranslation(
            "payment",
            domainEvent.getPaymentId().value(),
            "payment.processed",
            1,
            domainEvent.getOrderId().value(),
            message.toByteArray()
        );
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof PaymentProcessedEvent;
    }

    private Status mapStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case SUCCESSFUL -> Status.SUCCESSFUL;
            case FAILED -> Status.FAILED;
            case CANCELLED -> Status.CANCELLED;
            case PENDING -> Status.PENDING;
        };
    }
}

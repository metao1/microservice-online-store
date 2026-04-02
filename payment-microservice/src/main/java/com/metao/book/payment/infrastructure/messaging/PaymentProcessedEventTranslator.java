package com.metao.book.payment.infrastructure.messaging;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.metao.book.payment.domain.model.event.PaymentProcessedEvent;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.shared.OrderPaymentUpdatedEvent;
import com.metao.book.shared.Status;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.ProtobufDomainTranslator;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessedEventTranslator implements ProtobufDomainTranslator {

    @Override
    public Message translate(DomainEvent event) {
        PaymentProcessedEvent domainEvent = (PaymentProcessedEvent) event;
        return OrderPaymentUpdatedEvent.newBuilder()
            .setId(domainEvent.getEventId())
            .setPaymentId(domainEvent.getPaymentId().value())
            .setOrderId(domainEvent.getOrderId().value())
            .setStatus(mapStatus(domainEvent.getStatus()))
            .setUpdatedTime(Timestamp.newBuilder()
                .setSeconds(domainEvent.getOccurredOn().atZone(java.time.ZoneOffset.UTC).toEpochSecond())
                .setNanos(domainEvent.getOccurredOn().getNano())
                .build())
            .build();
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof PaymentProcessedEvent;
    }

    private Status mapStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case SUCCESSFUL -> Status.SUCCESSFUL;
            case FAILED -> Status.FAILED;
            default -> Status.FAILED; // PENDING, CANCELLED fallback
        };
    }
}

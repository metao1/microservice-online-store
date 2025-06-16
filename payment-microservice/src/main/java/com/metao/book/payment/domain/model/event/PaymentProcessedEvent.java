package com.metao.book.payment.domain.model.event;

import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Domain event raised when a payment is successfully processed
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends DomainEvent {

    private final PaymentId paymentId;
    private final OrderId orderId;
    private final Money amount;
    private final PaymentStatus status;

    public PaymentProcessedEvent(
        @NonNull PaymentId paymentId,
        @NonNull OrderId orderId,
        @NonNull Money amount,
        @NonNull PaymentStatus status
    ) {
        super(LocalDateTime.now());
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    @Override
    public String getEventType() {
        return "PaymentProcessed";
    }
}

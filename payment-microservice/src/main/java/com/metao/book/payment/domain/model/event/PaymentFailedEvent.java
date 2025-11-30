package com.metao.book.payment.domain.model.event;

import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Domain event raised when a payment fails
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends DomainEvent {

    private final PaymentId paymentId;
    private final OrderId orderId;
    private final Money amount;
    private final String failureReason;

    public PaymentFailedEvent(
        @NonNull PaymentId paymentId,
        @NonNull OrderId orderId,
        @NonNull Money amount,
        @NonNull String failureReason
    ) {
        super(Instant.now());
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.failureReason = failureReason;
    }

    @Override
    public String getEventType() {
        return "PaymentFailed";
    }
}

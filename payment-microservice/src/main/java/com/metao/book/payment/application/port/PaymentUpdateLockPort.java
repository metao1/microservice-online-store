package com.metao.book.payment.application.port;

import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import java.util.Optional;

public interface PaymentUpdateLockPort {

    Optional<PaymentAggregate> findByIdForUpdate(PaymentId paymentId);
}

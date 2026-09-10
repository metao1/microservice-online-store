package com.metao.book.payment.application.port;

import com.metao.book.payment.domain.model.valueobject.OrderId;

public interface PaymentCreationLockPort {

    void lock(OrderId orderId);
}

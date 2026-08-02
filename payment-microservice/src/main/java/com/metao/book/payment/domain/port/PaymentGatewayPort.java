package com.metao.book.payment.domain.port;

import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;

public interface PaymentGatewayPort {

    PaymentAuthorizationResult authorize(PaymentAggregate payment);

    record PaymentAuthorizationResult(boolean successful, String failureReason) {

        public static PaymentAuthorizationResult success() {
            return new PaymentAuthorizationResult(true, null);
        }

        public static PaymentAuthorizationResult failure(String reason) {
            return new PaymentAuthorizationResult(false, reason);
        }
    }
}

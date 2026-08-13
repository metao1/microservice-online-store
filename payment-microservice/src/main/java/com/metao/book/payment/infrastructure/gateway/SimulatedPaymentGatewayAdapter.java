package com.metao.book.payment.infrastructure.gateway;

import com.metao.book.payment.domain.port.PaymentGatewayPort;
import com.metao.book.shared.architecture.OutboundAdapter;
import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@OutboundAdapter
@Profile("!real-payment-gateway")
public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public PaymentAuthorizationResult authorize(PaymentAggregate payment) {
        return PaymentAuthorizationResult.success();
    }
}

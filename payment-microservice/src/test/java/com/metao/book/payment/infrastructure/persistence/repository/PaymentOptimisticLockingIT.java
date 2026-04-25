package com.metao.book.payment.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentMethod;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.shared.domain.financial.Money;
import com.metao.shared.test.KafkaContainer;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentOptimisticLockingIT extends KafkaContainer {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void staleAggregateSaveShouldFailWithOptimisticLock() {
        // Seed one payment row.
        PaymentId paymentId = PaymentId.of(UUID.randomUUID().toString());
        OrderId orderId = OrderId.of("order-" + UUID.randomUUID());
        PaymentAggregate created = new PaymentAggregate(
            paymentId,
            orderId,
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(12.10)),
            PaymentMethod.creditCard("**** **** **** 4242")
        );
        paymentRepository.saveAndFlush(created);

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // TX-A: read stale snapshot (version N).
        PaymentAggregate stale = tx.execute(status ->
            paymentRepository.findById(paymentId).orElseThrow()
        );

        // TX-B: update same row and commit (version N+1).
        tx.executeWithoutResult(status -> {
            PaymentAggregate fresh = paymentRepository.findById(paymentId).orElseThrow();
            fresh.cancel();
            paymentRepository.saveAndFlush(fresh);
        });

        // TX-A: try saving stale snapshot -> optimistic-lock conflict.
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            stale.cancel();
            paymentRepository.saveAndFlush(stale);
        })).isInstanceOf(OptimisticLockingFailureException.class);
    }
}

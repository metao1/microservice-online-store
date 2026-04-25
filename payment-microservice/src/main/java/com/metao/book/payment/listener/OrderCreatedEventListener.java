package com.metao.book.payment.listener;

import com.metao.book.payment.application.usecase.HandleOrderCreatedEventCommand;
import com.metao.book.payment.application.usecase.HandleOrderCreatedEventUseCase;
import com.metao.book.shared.OrderCreatedEvent;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final HandleOrderCreatedEventUseCase handleOrderCreatedEventUseCase;
    private final MeterRegistry meterRegistry;

    @Timed(value = "payment.listener.order-created")
    @KafkaListener(
        id = "${kafka.topic.order-created.id}",
        topics = "${kafka.topic.order-created.name}",
        groupId = "${kafka.topic.order-created.group-id}",
        containerFactory = "orderCreatedEventKafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent orderEvent, Acknowledgment acknowledgment) {
        if (orderEvent.hasCreateTime()) {
            Instant createdAt = Instant.ofEpochSecond(
                orderEvent.getCreateTime().getSeconds(),
                orderEvent.getCreateTime().getNanos()
            );
            long lagMs = Duration.between(createdAt, Instant.now()).toMillis();
            if (lagMs >= 0) {
                var summary = meterRegistry.summary("payment.listener.order-created.event-lag.ms");
                summary.record(lagMs);
            }
        }

        AggregatedAmount aggregatedAmount = resolveAmount(orderEvent);
        handleOrderCreatedEventUseCase.handle(new HandleOrderCreatedEventCommand(
            orderEvent.getId(),
            orderEvent.getId(),
            aggregatedAmount.amount(),
            aggregatedAmount.currency()
        ));
        acknowledgment.acknowledge();
    }

    private AggregatedAmount resolveAmount(OrderCreatedEvent orderEvent) {
        if (orderEvent.getItemsCount() == 0) {
            throw new IllegalArgumentException("OrderCreatedEvent must contain at least one item");
        }

        String currency = orderEvent.getItems(0).getCurrency();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreatedEvent.OrderItem item : orderEvent.getItemsList()) {
            if (!Objects.equals(currency, item.getCurrency())) {
                throw new IllegalArgumentException(
                    "OrderCreatedEvent contains mixed currencies: " + currency + " and " + item.getCurrency());
            }
            total = total.add(BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return new AggregatedAmount(total, currency);
    }

    private record AggregatedAmount(BigDecimal amount, String currency) {
    }
}

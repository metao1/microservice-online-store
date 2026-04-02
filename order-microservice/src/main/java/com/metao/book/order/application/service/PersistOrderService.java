package com.metao.book.order.application.service;

import com.metao.book.order.application.port.ProcessedOrderCreatedEventPort;
import com.metao.book.order.application.usecase.PersistOrderUseCase;
import com.metao.book.order.domain.event.OrderCreatedEvent;
import com.metao.book.order.domain.event.OrderCreatedEventItem;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.repository.OrderRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistOrderService implements PersistOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProcessedOrderCreatedEventPort processedOrderCreatedEventPort;

    @Override
    @Transactional
    public void persistOrder(OrderCreatedEvent event) {
        String eventId = toProcessedEventId(event);
        if (!processedOrderCreatedEventPort.markProcessed(eventId)) {
            log.info("OrderCreatedEvent {} already processed; skipping duplicate.", eventId);
            return;
        }

        orderRepository.findById(event.orderId())
            .ifPresentOrElse(order -> mergeOrderItems(order, event), () -> orderRepository.save(OrderAggregate.from(event)));
    }

    private void mergeOrderItems(OrderAggregate order, OrderCreatedEvent event) {
        AtomicBoolean changed = new AtomicBoolean(false);

        for (OrderCreatedEventItem item : event.items()) {
            if (order.hasItem(item.productSku())) {
                log.info("Order {} already contains sku {}; skipping duplicate order-created event item.",
                    event.orderId().value(), item.productSku().value());
                continue;
            }

            order.addItem(item.productSku(), item.productTitle(), item.quantity(), item.unitPrice());
            changed.set(true);
        }

        if (changed.get()) {
            orderRepository.save(order);
        }
    }

    private String toProcessedEventId(OrderCreatedEvent event) {
        return event.orderId().value();
    }
}

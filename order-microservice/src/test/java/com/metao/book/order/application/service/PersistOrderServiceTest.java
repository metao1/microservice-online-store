package com.metao.book.order.application.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.port.ProcessedOrderCreatedEventPort;
import com.metao.book.order.domain.event.OrderCreatedEvent;
import com.metao.book.order.domain.event.OrderCreatedEventItem;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersistOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProcessedOrderCreatedEventPort processedOrderCreatedEventPort;

    @InjectMocks
    private PersistOrderService persistOrderService;

    @Test
    void persistOrderSkipsDuplicateProcessedEvent() {
        OrderCreatedEvent event = buildEvent("order-1");

        when(processedOrderCreatedEventPort.markProcessed("order-1")).thenReturn(false);

        persistOrderService.persistOrder(event);

        verify(orderRepository, never()).findById(event.orderId());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(OrderAggregate.class));
    }

    @Test
    void persistOrderSavesNewOrderWhenProcessedForTheFirstTime() {
        OrderCreatedEvent event = buildEvent("order-1");

        when(processedOrderCreatedEventPort.markProcessed("order-1")).thenReturn(true);
        when(orderRepository.findById(event.orderId())).thenReturn(Optional.empty());

        persistOrderService.persistOrder(event);

        verify(orderRepository).save(org.mockito.ArgumentMatchers.any(OrderAggregate.class));
    }

    private OrderCreatedEvent buildEvent(String orderId) {
        return new OrderCreatedEvent(
            OrderId.of(orderId),
            UserId.of("user-1"),
            java.util.List.of(
                new OrderCreatedEventItem(
                    ProductSku.of("SKU-1"),
                    ProductTitle.of("Book 1"),
                    Quantity.of(BigDecimal.ONE),
                    Money.of(Currency.getInstance("EUR"), BigDecimal.TEN)
                ),
                new OrderCreatedEventItem(
                    ProductSku.of("SKU-2"),
                    ProductTitle.of("Book 2"),
                    Quantity.of(BigDecimal.TWO),
                    Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(5))
                )
            ),
            OrderStatus.CREATED,
            Instant.parse("2026-04-01T10:15:30Z"),
            Instant.parse("2026-04-01T10:15:30Z")
        );
    }
}

package com.metao.book.order.infrastructure.messaging.translator;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderCreatedEventTranslatorTest {

    private final OrderCreatedEventTranslator translator = new OrderCreatedEventTranslator();

    @Test
    void translateMapsAggregatedOrderItemPayload() {
        OrderItem firstOrderItem = new OrderItem(
            ProductSku.of("SKU-1"),
            ProductTitle.of("Book 1"),
            Quantity.of(BigDecimal.TWO),
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(12.99))
        );
        OrderItem secondOrderItem = new OrderItem(
            ProductSku.of("SKU-2"),
            ProductTitle.of("Book 2"),
            Quantity.of(BigDecimal.ONE),
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(8.50))
        );
        Money subtotal = Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(34.48));
        DomainOrderCreatedEvent event = new DomainOrderCreatedEvent(
            OrderId.of("order-1"),
            UserId.of("user-1"),
            List.of(firstOrderItem, secondOrderItem),
            subtotal,
            Money.of(Currency.getInstance("EUR"), BigDecimal.ZERO),
            subtotal,
            new VAT(0),
            Instant.parse("2026-03-29T10:15:30Z")
        );

        OrderCreatedEvent translated = (OrderCreatedEvent) translator.translate(event);

        assertThat(translated.getId()).isEqualTo("order-1");
        assertThat(translated.getUserId()).isEqualTo("user-1");
        assertThat(translated.getItemsCount()).isEqualTo(2);
        assertThat(translated.getItemsList()).extracting(OrderCreatedEvent.OrderItem::getSku)
            .containsExactly("SKU-1", "SKU-2");
        assertThat(translated.getItems(0).getProductTitle()).isEqualTo("Book 1");
        assertThat(translated.getItems(0).getQuantity()).isEqualTo(2.0d);
        assertThat(translated.getItems(0).getPrice()).isEqualTo(12.99d);
        assertThat(translated.getItems(0).getCurrency()).isEqualTo("EUR");
        assertThat(translated.getStatus()).isEqualTo(OrderCreatedEvent.Status.CREATED);
    }
}
